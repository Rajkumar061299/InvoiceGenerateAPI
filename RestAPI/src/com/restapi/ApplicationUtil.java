package com.restapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;

public class ApplicationUtil {

	public static String printException(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter, true);
		throwable.printStackTrace(printWriter);
		printWriter.flush();
		stringWriter.flush();
		return stringWriter.toString();
	}

	public static String getAccessToken(String tokenCredentialsPayload) {

		try {
			HttpRequest postRequest = HttpRequest.newBuilder()
					.uri(new URI(ApplicationConstants.ACCESS_TOKEN_API))
					.header("Content-Type", "application/json")
					.POST(BodyPublishers.ofString(tokenCredentialsPayload))
					.build();

			HttpClient httpClient = HttpClient.newHttpClient();
			HttpResponse<String> postResponse = httpClient.send(postRequest,
					BodyHandlers.ofString());

			JsonObject responseObject = new JsonParser()
					.parse(postResponse.body()).getAsJsonObject();
			return responseObject.get("access_token").getAsString();

		} catch (Exception e) {
			System.err.println(printException(e));
		}
		return "";

	}

	public static void getPDFByInvoiceId(String accessToken, String invoiceId) {

		try {
			HttpRequest getRequest = HttpRequest.newBuilder()
					.uri(new URI(ApplicationConstants.INVOICE_GENERATE_API
							+ invoiceId))
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + accessToken).build();

			HttpClient httpClient = HttpClient.newHttpClient();

			HttpResponse<InputStream> getResponse = httpClient.send(getRequest,
					HttpResponse.BodyHandlers.ofInputStream());

			if (getResponse.statusCode() == 200) {
				InputStream responseBody = getResponse.body();
				Path filePath = Path.of("./RestAPI");

				FileOutputStream outputStream = new FileOutputStream(
						filePath.toFile());
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = responseBody.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.close();

				System.out.println("PDF file downloaded successfully.");
			} else {
				System.out.println(
						"Failed to download the PDF file. Unexpected response.");
			}
		} catch (Exception e) {
			System.err.println(printException(e));
		}

	}

}
