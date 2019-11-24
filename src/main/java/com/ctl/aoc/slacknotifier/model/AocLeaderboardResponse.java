package com.ctl.aoc.slacknotifier.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * This class represents the return data sent by AOC when querying a leaderboard
 */
@Getter
@Setter
@DynamoDBDocument
public class AocLeaderboardResponse {

    @JsonProperty("owner_id")
    private String ownerId;

    private String event;

    /**
     * A map from a memberId to {@link MemberEntry}
     */
    private Map<String, MemberEntry> members;
}
