package app.coronawarn.server.services.distribution.objectstore.client;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.ObjectStore;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Manages the instantiation of the {@link ObjectStoreClient} bean.
 */
@Configuration
@EnableRetry
public class ObjectStorePublishingConfig {

  private static final Region DEFAULT_REGION = Region.EU_CENTRAL_1;

  @Bean(name = "publish-s3")
  public ObjectStoreClient createObjectStoreClient(DistributionServiceConfig distributionServiceConfig) {
    return createClient(distributionServiceConfig.getObjectStore(),
        distributionServiceConfig.getDccRevocation().getDccListPath());
  }

  private ObjectStoreClient createClient(final ObjectStore objectStore, final String dccListPath) {
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(objectStore.getAccessKey(), objectStore.getSecretKey()));
    String endpoint = removeTrailingSlash(objectStore.getEndpoint()) + ":" + objectStore.getPort();

    return new S3ClientWrapper(S3Client.builder()
        .region(DEFAULT_REGION)
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(credentialsProvider)
        .build(), dccListPath);
  }

  private String removeTrailingSlash(String string) {
    return string.endsWith("/") ? string.substring(0, string.length() - 1) : string;
  }

  /**
   * Creates a {@link ThreadPoolTaskExecutor}, which is used to submit object store upload tasks.
   *
   * @param distributionServiceConfig DistributionServiceConfig containing object store attributes
   * @return ThreadPoolTaskExecutor
   */
  @Bean
  public ThreadPoolTaskExecutor createExecutor(DistributionServiceConfig distributionServiceConfig) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(distributionServiceConfig.getObjectStore().getMaxNumberOfS3Threads());
    executor.setMaxPoolSize(distributionServiceConfig.getObjectStore().getMaxNumberOfS3Threads());
    executor.setThreadNamePrefix("object-store-operation-worker-");
    executor.initialize();
    return executor;
  }
}
