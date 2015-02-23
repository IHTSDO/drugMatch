package org.ihtsdo.sct.drugmatch.verification.service.healthterm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ihtsdo.sct.drugmatch.exception.DrugMatchConfigurationException;
import org.ihtsdo.sct.drugmatch.properties.DrugMatchProperties;
import org.ihtsdo.sct.drugmatch.verification.service.VerificationService;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.ConceptSearchResultDescriptor;
import org.ihtsdo.sct.drugmatch.verification.service.healthterm.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HealthTerm implementation of {@link VerificationService}.
 * @author dev-team@carecom.dk
 */
public class VerificationServiceImpl implements VerificationService {

	private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

	private static final TypeReference<List<ConceptDescriptor>> CONCEPT_DESCRIPTOR_TYPE_REFERENCE = new TypeReference<List<ConceptDescriptor>>() {
		// empty on purpose.
	};

	private static final TypeReference<List<ConceptSearchResultDescriptor>> CONCEPT_SEARCH_RESULT_DESCRIPTOR_TYPE_REFERENCE = new TypeReference<List<ConceptSearchResultDescriptor>>() {
		// empty on purpose.
	};

	private static final TypeReference<List<LogEntry>> LOG_ENTRY_TYPE_REFERENCE = new TypeReference<List<LogEntry>>() {
		// empty on purpose.
	};

	/**
	 * Empty, cause: unable to filter on namespace as the SNOMED CT international release now contains 1+ namespace.
	 */
	private static final Set<String> SNOMED_CT_NAMESPACE_IDS = Collections.<String>emptySet();

	private final CredentialsProvider credentialsProvider;

	private SSLConnectionSocketFactory customSslSocketFactory = null;

	private final Set<String> englishLocaleCodes = new TreeSet<>(Arrays.asList(new String[] {"en", "en-GB", "en-US"}));

	/**
	 * @throws DrugMatchConfigurationException
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public VerificationServiceImpl() throws DrugMatchConfigurationException, KeyManagementException, NoSuchAlgorithmException, IOException {
		String serviceUrl = DrugMatchProperties.getVerificationService();
		if (serviceUrl == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_SERVICE + "' isn't set!");
		}
		UrlValidator urlValidator = new UrlValidator(new String[] {"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
		if (!urlValidator.isValid(serviceUrl.toLowerCase(Locale.ENGLISH))) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_SERVICE + "' isn't a valid HTTP/HTTPS URL!");
		}
		String login = DrugMatchProperties.getVerificationLogin();
		if (login == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_LOGIN + "' isn't set!");
		}
		String password = DrugMatchProperties.getVerificationPassword();
		if (password == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.VERIFICATION_PASSWORD + "' isn't set!");
		}
		URL url = new URL(serviceUrl);
		int port;
		if (url.getPort() > 0) {
			port = url.getPort();
		} else {
			port = ("https".equals(url.getProtocol().toLowerCase(Locale.ENGLISH))) ? 443 : 80;
		}
		this.credentialsProvider = new BasicCredentialsProvider();
		this.credentialsProvider.setCredentials(
				new AuthScope(url.getHost(), port),
				new UsernamePasswordCredentials(login, password));
		// test if CA root is present in JRE, otherwise invoke SSL workaround
		try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
			HttpGet httpget = new HttpGet(url.getProtocol() + "://" + url.getHost() + ":" + port);
			log.debug("Executing request: {}", httpget.getRequestLine());
			try (CloseableHttpResponse response = httpclient.execute(httpget);) {
				log.debug("Executed request: {} status: {}", httpget.getRequestLine(), response.getStatusLine());
				EntityUtils.consume(response.getEntity());
			} catch (SSLHandshakeException e) {
				if ("sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target".equals(e.getMessage())) {
					log.debug("Unable to establish certificate trust chain, most likely cause: root certificate unavailable from JRE keystore. Proceeding with disabled TrustManager.");
					this.customSslSocketFactory = new SSLConnectionSocketFactory(initializeCerts());
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 *  Workaround for ex. StartCom CA root certificate is not included in the JRE's default keystore.
	 * @return {@link SSLContext}
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	private static SSLContext initializeCerts() throws KeyManagementException, NoSuchAlgorithmException {

		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(final X509Certificate[] certs,
						final String authType) {
					// empty on purpose
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] certs,
						final String authType) {
					// empty on purpose
				}
			}};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		return sc;
	}

	private CloseableHttpClient getHttpClient() {
		if (this.customSslSocketFactory == null) {
			return HttpClients.custom().setDefaultCredentialsProvider(this.credentialsProvider).build();
		} // else
		return HttpClients.custom().setDefaultCredentialsProvider(this.credentialsProvider).setSSLSocketFactory(this.customSslSocketFactory).build();
	}

	private static HttpGet getHttpGetJSON(final String path) throws DrugMatchConfigurationException {
		StringBuilder url = new StringBuilder(DrugMatchProperties.getVerificationService());
		if (!path.startsWith("/")) {
			url.append('/');
		}
		url.append(path);
		HttpGet httpget = new HttpGet(url.toString());
		httpget.setHeader("Accept", "application/json");
		return httpget;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getAttributeExactMatch(final Set<Long> attributeIds,
			final Set<Long> valueIds) throws DrugMatchConfigurationException, IOException {
		// construct path
		StringBuilder path = new StringBuilder("/webservice/restricted/v1.0/search/concept/attributeRelationExact?");
		// attribute (Relationship type ID)
		for (Long attributeId : attributeIds) {
			path.append("&attributeId=")
				.append(attributeId);
		}
		// value (conceptid2|target)
		for (Long valueId : valueIds) {
			path.append("&valueId=")
				.append(valueId);
		}
		// remove unneeded '&' for first parameter
		int i = path.indexOf("?&");
		if (i > -1) {
			path.deleteCharAt(i + 1);
		}
		// search
		return getConceptSearchResult(path.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptDescriptor> getConceptsByIds(final Set<Long> conceptIds) throws DrugMatchConfigurationException, IOException {
		try (CloseableHttpClient httpclient = getHttpClient();) {
			HttpGet httpget = getHttpGetConceptsByIds(conceptIds);
			try (CloseableHttpResponse response = httpclient.execute(httpget);) {
				log.debug("Executed request: {} status: {}", httpget.getRequestLine(), response.getStatusLine());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode results = mapper.readTree(response.getEntity().getContent()).get("record");
					List<ConceptDescriptor> result = mapper.readValue(results.traverse(), CONCEPT_DESCRIPTOR_TYPE_REFERENCE);
					EntityUtils.consume(response.getEntity());
					if (result.size() != conceptIds.size()) {
						log.debug("Request returned: {} expected: {} result(s)!",
								result.size(),
								conceptIds.size());
					}
					return result;
				} // else
				StringBuilder sb = new StringBuilder();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
						CharEncoding.UTF_8));) {
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					ObjectMapper mapper = new ObjectMapper();
					JsonNode logEntries = mapper.readTree(sb.toString()).get("logEntry");
					List<LogEntry> result = mapper.readValue(logEntries.traverse(), LOG_ENTRY_TYPE_REFERENCE);
					EntityUtils.consume(response.getEntity());
					if (result.isEmpty()) {
						throw new IOException("Unable to retrieve Concept by ID, cause: HTTP status code " + response.getStatusLine().getStatusCode());
					} // else
					throw new IOException(result.iterator().next().toString());
				} catch (JsonParseException e) {
					throw new IOException("Unable to retrieve Concept by ID, cause: HTTP status code " + response.getStatusLine().getStatusCode() +
							" response content: " + sb.toString());
				}
			}
		}
	}

	/**
	 * Common logic for generating a Concept ID GET.
	 * @param conceptIds
	 * @return {@link HttpGet}
	 * @throws DrugMatchConfigurationException
	 */
	private static HttpGet getHttpGetConceptsByIds(Set<Long> conceptIds) throws DrugMatchConfigurationException {
		// construct path
		StringBuilder path = new StringBuilder("/webservice/restricted/v1.0/lookup/concept/byId?");
		// attribute (Relationship type ID)
		for (Long conceptId : conceptIds) {
			path.append("&conceptId=")
				.append(conceptId);
		}
		// remove unneeded '&' for first parameter
		int i = path.indexOf("?&");
		if (i > -1) {
			path.deleteCharAt(i + 1);
		}
		return getHttpGetJSON(path.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConceptsByIdsUrl(Set<Long> conceptIds) throws DrugMatchConfigurationException {
		return getHttpGetConceptsByIds(conceptIds).getURI().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getDoseFormExactEnglishTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		return getDoseFormExactTermMatch(
				SNOMED_CT_NAMESPACE_IDS,
				query,
				this.englishLocaleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getDoseFormExactNationalTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		String nationalNamespaceId = DrugMatchProperties.getNationalNamespaceId();
		if (nationalNamespaceId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.EXTENSION_NAMESPACE_ID + "' isn't set!");
		} // else
		return getDoseFormExactTermMatch(
				Collections.singleton(nationalNamespaceId),
				query,
				Collections.<String>emptySet());
	}

	/**
	 * Common logic for dose form exact term match
	 * @param namespaceIds
	 * @param query
	 * @param localeCodes
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private List<ConceptSearchResultDescriptor> getDoseFormExactTermMatch(final Set<String> namespaceIds,
			final String query,
			final Set<String> localeCodes) throws DrugMatchConfigurationException, IOException {
		Long constraintId = DrugMatchProperties.getConstraintIdDoseForm();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_DOSE_FORM + "' isn't set!");
		} // else
		return getExactTermMatch(
				namespaceIds,
				Collections.singleton(constraintId),
				localeCodes,
				query);
	}

	/**
	 * Common logic for exact term match
	 * @param namespaceIds
	 * @param query
	 * @param localeCodes
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private List<ConceptSearchResultDescriptor> getExactTermMatch(final Set<String> namespaceIds,
			final Set<Long> constraintIds,
			final Set<String> localeCodes,
			final String query) throws DrugMatchConfigurationException, IOException {
		// construct path
		StringBuilder path = new StringBuilder("/webservice/restricted/v1.0/search/concept/exactSearch?query=");
		path.append(URLEncoder.encode(query, CharEncoding.UTF_8));
		// top point constraints
		for (Long constraintId : constraintIds) {
			path.append("&constraintId=")
				.append(constraintId);
		}
		// namespaces
		for (String namespaceId : namespaceIds) {
			path.append("&namespaceId=")
				.append(URLEncoder.encode(namespaceId, CharEncoding.UTF_8));
		}
		// locale codes
		for (String localeCode : localeCodes) {
			path.append("&contentLocaleCode=")
				.append(URLEncoder.encode(localeCode, CharEncoding.UTF_8));
		}
		// search
		return getConceptSearchResult(path.toString());
	}

	/**
	 * @param path
	 * @return {@link List}({@link ConceptSearchResultDescriptor}), for the given path.
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private List<ConceptSearchResultDescriptor> getConceptSearchResult(final String path) throws DrugMatchConfigurationException, IOException {
		try (CloseableHttpClient httpclient = getHttpClient();) {
			HttpGet httpget = getHttpGetJSON(path);
			try (CloseableHttpResponse response = httpclient.execute(httpget);) {
				log.debug("Executed request: {} status: {}", httpget.getRequestLine(), response.getStatusLine());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode results = mapper.readTree(response.getEntity().getContent()).get("record");
					List<ConceptSearchResultDescriptor> result = mapper.readValue(results.traverse(), CONCEPT_SEARCH_RESULT_DESCRIPTOR_TYPE_REFERENCE);
					EntityUtils.consume(response.getEntity());
					log.debug("Request returned: {} result(s).",
							result.size());
					return result;
				} // else
				StringBuilder sb = new StringBuilder();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
						CharEncoding.UTF_8));) {
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					ObjectMapper mapper = new ObjectMapper();
					JsonNode logEntries = mapper.readTree(sb.toString()).get("logEntry");
					List<LogEntry> result = mapper.readValue(logEntries.traverse(), LOG_ENTRY_TYPE_REFERENCE);
					EntityUtils.consume(response.getEntity());
					if (result.isEmpty()) {
						throw new IOException("Unable to search for exact match, cause: HTTP status code " + response.getStatusLine().getStatusCode());
					} // else
					throw new IOException(result.iterator().next().toString());
				} catch (JsonParseException e) {
					throw new IOException("Unable to search for exact match, cause: HTTP status code " + response.getStatusLine().getStatusCode() +
							" response content: " + sb.toString());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getSubstanceExactEnglishTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		return getSubstanceExactTermMatch(
				SNOMED_CT_NAMESPACE_IDS,
				query,
				this.englishLocaleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getSubstanceExactNationalTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		String nationalNamespaceId = DrugMatchProperties.getNationalNamespaceId();
		if (nationalNamespaceId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.EXTENSION_NAMESPACE_ID + "' isn't set!");
		} // else
		return getSubstanceExactTermMatch(
				Collections.singleton(nationalNamespaceId),
				query,
				Collections.<String>emptySet());
	}

	/**
	 * Common logic for substance exact term match
	 * @param namespaceIds
	 * @param query
	 * @param localeCodes
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private List<ConceptSearchResultDescriptor> getSubstanceExactTermMatch(final Set<String> namespaceIds,
			final String query,
			final Set<String> localeCodes) throws DrugMatchConfigurationException, IOException {
		Long constraintId = DrugMatchProperties.getConstraintIdSubstance();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_SUBSTANCE + "' isn't set!");
		} // else
		return getExactTermMatch(
				namespaceIds,
				Collections.singleton(constraintId),
				localeCodes,
				query);
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getUnitExactEnglishTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		return getUnitExactTermMatch(
				SNOMED_CT_NAMESPACE_IDS,
				query,
				this.englishLocaleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<ConceptSearchResultDescriptor> getUnitExactNationalTermMatch(final String query) throws DrugMatchConfigurationException, IOException {
		String nationalNamespaceId = DrugMatchProperties.getNationalNamespaceId();
		if (nationalNamespaceId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.EXTENSION_NAMESPACE_ID + "' isn't set!");
		} // else
		return getUnitExactTermMatch(
				Collections.singleton(nationalNamespaceId),
				query,
				Collections.<String>emptySet());
	}

	/**
	 * Common logic for unit exact term match
	 * @param namespaceIds
	 * @param query
	 * @param localeCodes
	 * @return {@link List}({@link ConceptSearchResultDescriptor})
	 * @throws DrugMatchConfigurationException
	 * @throws IOException
	 */
	private List<ConceptSearchResultDescriptor> getUnitExactTermMatch(final Set<String> namespaceIds,
			final String query,
			final Set<String> localeCodes) throws DrugMatchConfigurationException, IOException {
		Long constraintId = DrugMatchProperties.getConstraintIdUnit();
		if (constraintId == null) {
			throw new DrugMatchConfigurationException("Unable to proceed, cause: '" + DrugMatchProperties.CONSTRAINT_ID_UNIT + "' isn't set!");
		} // else
		return getExactTermMatch(
				namespaceIds,
				Collections.singleton(constraintId),
				localeCodes,
				query);
	}
}
