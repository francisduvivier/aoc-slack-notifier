package com.ctl.aoc.slacknotifier.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.ctl.aoc.slacknotifier.model.AocLeaderboardResponse;
import com.ctl.aoc.slacknotifier.model.PollingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.dynamodb.DynaliteContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class PollingEventDaoImplTest {

    @Container
    public DynaliteContainer dynamoDB = new DynaliteContainer();

    private PollingEventDao pollingEventDao;

    @BeforeEach
    void setUp() throws InterruptedException {
        final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(dynamoDB.getEndpointConfiguration())
                .withCredentials(dynamoDB.getCredentials())
                .build();
        pollingEventDao = new PollingEventDaoImpl(dynamoDBClient, Clock.systemUTC());

        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        CreateTableRequest req = mapper.generateCreateTableRequest(PollingEvent.class);
        // Table provision throughput is still required since it cannot be specified in your POJO
        req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        // Fire off the CreateTableRequest using the low-level client
        final CreateTableResult result = dynamoDBClient.createTable(req);

        System.out.println("Waiting for table creation");
        final long t = System.currentTimeMillis();
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        dynamoDB.getTable(result.getTableDescription().getTableName()).waitForActive();
        System.out.println(String.format("Table created after %d", System.currentTimeMillis() - t));
    }

    @Test
    void saveAndRetrieve() throws IOException {
        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("leaderBoardResponse.json");
        final ObjectMapper mapper = new ObjectMapper();
        final AocLeaderboardResponse aocLeaderboardResponse = mapper.readValue(inputStream, AocLeaderboardResponse.class);

        final Instant now = Instant.now();
        final PollingEvent pollingEvent = PollingEvent.builder()
                .leaderBoardId("251939")
                .timestamp(now.toEpochMilli())
                .yearEvent("2018")
                .data(aocLeaderboardResponse)
                .build();

        pollingEventDao.save(pollingEvent);

        final Optional<PollingEvent> fetch = pollingEventDao.findLatest("251939", "2018");

        assertThat(fetch).hasValueSatisfying(event -> {
            assertThat(event.getTimestamp()).isEqualTo(now.toEpochMilli());
        });
    }
}