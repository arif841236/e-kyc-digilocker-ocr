package com.ekyc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ekyc.exception.KycException;
import com.ekyc.model.ESignRequest;
import com.ekyc.model.common.ESignResponse;
import com.ekyc.service.IDigilockerService;
import com.ekyc.service.IPdfService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfServiceImpl implements IPdfService {

	@Autowired
	IDigilockerService digilockerService;

	@Autowired
	Gson gson;

	@Value("${alias.key}")
	String aliasKey;

	@Value("${sign.password}")
	String password;

	@Value("${sign.signAuthority}")
	String signAuth;

	@Value("${consent.error}")
	String consentError;

	@Value("${logo.base64}")
	String logoImage;

	@Override
	public ESignResponse savePdfFile(ESignRequest pdfRequest, String token) throws Exception {
		if(!pdfRequest.getConsent().equalsIgnoreCase("y") && !pdfRequest.getConsent().equalsIgnoreCase("n") ) {
			throw new KycException(consentError);
		}

		if(pdfRequest.getConsent().equalsIgnoreCase("n")) {
			throw new KycException("Digitally signed not possible with consent "+pdfRequest.getConsent());
		}

		String sign = getName(token,pdfRequest.getAadhaarNo());
		log.info(sign);
		pdfRequest.setIssueTo(sign);

		byte[] decode = Base64.getDecoder().decode(pdfRequest.getPdfBase64());

		InputStream inputStream = new ByteArrayInputStream(decode);

		ESignResponse digitalSignature = digitalSignature(inputStream,pdfRequest);

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("statusCode", 200);
		jsonObject.addProperty("message", "Add digital signature successfully");

		return digitalSignature;
	}

	private String getName(String token, String aNumber) {
		Object adharDetails = digilockerService.getAdharDetails(token);

		if(adharDetails == null) {
			throw new KycException("Digital signature not possible due to aadhaar details null");
		}
		String kycRes = "KycRes";
		String certificate = "Certificate";

		JsonObject fromJson = gson.fromJson(gson.toJson(adharDetails), JsonObject.class);

		if(fromJson == null ||fromJson.get(certificate) == null) {
			throw new KycException("Digitally signed not possible due to aadhaar data is null");
		}
		JsonElement certificateData = fromJson.get(certificate).getAsJsonObject().get("CertificateData");

		if(certificateData == null || certificateData.getAsJsonObject().get(kycRes) == null) {
			throw new KycException("Digitally signed not possible due to certificate data is null");
		}
		JsonElement jsonElement = certificateData.getAsJsonObject().get(kycRes).getAsJsonObject().get("UidData");
		if(jsonElement == null) {
			throw new KycException("Digitally signed not possible due to Uid data is null");
		}
		JsonObject asJsonObject = jsonElement.getAsJsonObject();

		if(asJsonObject == null || asJsonObject.get("Poi") == null) {
			throw new KycException("Digitally signed not possible due to poi element is null");
		}

		JsonObject poi = asJsonObject.get("Poi").getAsJsonObject();

		if(poi==null || poi.get("name")==null) {
			throw new KycException("Digitally signed not possible due to poi is null");
		}
		String maskedAadhaarNumber = asJsonObject.get("uid").getAsString();
        if(aNumber != null) {
        	checkAadhaar(aNumber,maskedAadhaarNumber);
        }
		return poi.get("name").getAsString();
	}

	private void checkAadhaar(String aNumber, String maskedAadhaarNumber) {
		if(!maskedAadhaarNumber.contains(aNumber.substring(8))) {
			throw new KycException("Aadhaar number is not match please enter correct aadhaar number.");
		}
	}

	@SuppressWarnings("resource")
	public ESignResponse digitalSignature(InputStream inputStream, ESignRequest pdfRequest)throws Exception {

		String outputFile = "./output.pdf";
		PdfSigner.CryptoStandard cryptoStandardSubFilter = PdfSigner.CryptoStandard.CMS;
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		Security.addProvider(bouncyCastleProvider);

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		File file = getJksFile(pdfRequest);
		keyStore.load(new FileInputStream(file), password.toCharArray());
		String alias = keyStore.aliases().nextElement();
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
		Certificate[] certificateChain = keyStore.getCertificateChain(alias);
		PdfReader pdfReader = new PdfReader(inputStream);
		PdfWriter pdfWriter = new PdfWriter(outputFile);
		int numberOfPages = new PdfDocument(pdfReader).getNumberOfPages();

		int height = 0;
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyy.MM.dd HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = dateFormat.format(now);

		String dSign = "eSigned by "+pdfRequest.getIssueTo()+"\n"+"Date: "+date+"\n";

		if(pdfRequest.getReason()!=null && !pdfRequest.getReason().isEmpty()) {
			height = 80;
			dSign = dSign.concat("Reason: "+pdfRequest.getReason()+"\n"+"Signed with "+signAuth);
		}
		else {
			height = 70;
			dSign = dSign.concat("Signed with "+signAuth);
		}

		byte[] decode = Base64.getDecoder().decode(logoImage);
		ImageData imageData = ImageDataFactory.create(decode);

		for(int i =1; i<=numberOfPages; i++) {
			if(i!=1) {
				inputStream =new FileInputStream(outputFile);
				pdfReader = new PdfReader(inputStream);
				pdfWriter = new PdfWriter(outputFile);
			}

			PdfSigner pdfSigner = new PdfSigner(pdfReader, pdfWriter, false);

			// Create the signature appearance
			PdfSignatureAppearance pdfSignatureAppearance = pdfSigner.getSignatureAppearance();

			if(pdfRequest.getReason()!=null && !pdfRequest.getReason().isEmpty()) {
				pdfSignatureAppearance.setReason(pdfRequest.getReason());
			}

			pdfSignatureAppearance.setLayer2Text(dSign);
			Rectangle rectangle = new Rectangle(420, 20, 160, height);
			rectangle.applyMargins(10, 0, 0, 10, false);
			rectangle.moveDown(25);

			pdfSignatureAppearance.setPageRect(rectangle);
			pdfSignatureAppearance.setLayer2FontSize(10);

			pdfSignatureAppearance.setCertificate(certificateChain[0]);
			pdfSignatureAppearance.setPageNumber(i);

		
			int[] arr = {0x03, 0x20};
			imageData.setTransparency(arr);
			
			pdfSignatureAppearance.setImage(imageData);
			pdfSignatureAppearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
			IExternalSignature iExternalSignature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, bouncyCastleProvider.getName());
			IExternalDigest iExternalDigest = new BouncyCastleDigest();

			// Sign the document using the detached mode, CMS, or CAdES equivalent.
			pdfSigner.signDetached(iExternalDigest, iExternalSignature, certificateChain, null, null, null, 0, cryptoStandardSubFilter);

			pdfReader.close();
			pdfWriter.close();
			inputStream.close();
		}

		return getPdfResponse(outputFile,pdfRequest.getSource());
	}

	private ESignResponse getPdfResponse(String outputFile, String source) throws IOException {
		String encode = "";
		try (FileInputStream fileInputStream = new FileInputStream(outputFile)) {
			byte[] readAllBytes = fileInputStream.readAllBytes();
			encode = Base64.getEncoder().encodeToString(readAllBytes);
		}

		return ESignResponse.builder()
				.signedPDF(encode)
				.statusCode(101)
				.initiatedBy(source)
				.build();
	}

	private File getJksFile(ESignRequest pdfRequest) throws GeneralSecurityException, IOException, OperatorCreationException{
		KeyStore keyStore = KeyStore.getInstance("JKS");

		char[] passwordArray = password.toCharArray();

		keyStore.load(null, passwordArray);

		// Generate key pair
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		SecureRandom secureRandom = new SecureRandom();
		keyPairGenerator.initialize(2048, secureRandom);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// Create X500Name objects for issuer and subject
		X500Name issuer = new X500Name("CN="+signAuth);
		X500Name subject = new X500Name("CN="+pdfRequest.getIssueTo());

		// Create certificate serial number and validity dates
		BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
		Date startDate = new Date();

		Date endDate = new Date(startDate.getTime() + 30 * 24 * 60 * 60 * 1000L);

		// Create X509v3CertificateBuilder object
		X509v3CertificateBuilder builder = new X509v3CertificateBuilder  (
				issuer, serial, startDate, endDate, subject, SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

		// Initialize the OperatorHelper class
		// Create content signer
		ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());

		// Build certificate and convert to X509Certificate object
		X509CertificateHolder holder = builder.build(signer);

		X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

		// Add key entry to keystore
		keyStore.setKeyEntry(aliasKey, keyPair.getPrivate(), passwordArray, new Certificate[]{cert});

		// Write keystore to output stream and return it
		File file=new File("./mykeystore.jks");
		OutputStream out = new FileOutputStream(file);
		keyStore.store(out, passwordArray);
		out.close();

		return file;
	}	
}