package com.enterprise.audit.logging.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuditResultTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAuditResultValues() {
        assertEquals("SUCCESS", AuditResult.SUCCESS.name());
        assertEquals("FAILURE", AuditResult.FAILURE.name());
        assertEquals("DENIED", AuditResult.DENIED.name());
        assertEquals("INVALID", AuditResult.INVALID.name());
        assertEquals("TIMEOUT", AuditResult.TIMEOUT.name());
        assertEquals("CANCELLED", AuditResult.CANCELLED.name());
        assertEquals("UNKNOWN", AuditResult.UNKNOWN.name());
    }

    @Test
    public void testAuditResultOrdinals() {
        assertEquals(0, AuditResult.SUCCESS.ordinal());
        assertEquals(1, AuditResult.FAILURE.ordinal());
        assertEquals(2, AuditResult.DENIED.ordinal());
        assertEquals(3, AuditResult.INVALID.ordinal());
        assertEquals(4, AuditResult.TIMEOUT.ordinal());
        assertEquals(5, AuditResult.CANCELLED.ordinal());
        assertEquals(6, AuditResult.UNKNOWN.ordinal());
    }

    @Test
    public void testAuditResultValueOf() {
        assertEquals(AuditResult.SUCCESS, AuditResult.valueOf("SUCCESS"));
        assertEquals(AuditResult.FAILURE, AuditResult.valueOf("FAILURE"));
        assertEquals(AuditResult.DENIED, AuditResult.valueOf("DENIED"));
        assertEquals(AuditResult.INVALID, AuditResult.valueOf("INVALID"));
        assertEquals(AuditResult.TIMEOUT, AuditResult.valueOf("TIMEOUT"));
        assertEquals(AuditResult.CANCELLED, AuditResult.valueOf("CANCELLED"));
        assertEquals(AuditResult.UNKNOWN, AuditResult.valueOf("UNKNOWN"));
    }

    @Test
    public void testAuditResultValuesArray() {
        AuditResult[] values = AuditResult.values();
        assertEquals(7, values.length);
        
        assertArrayEquals(new AuditResult[]{
                AuditResult.SUCCESS,
                AuditResult.FAILURE,
                AuditResult.DENIED,
                AuditResult.INVALID,
                AuditResult.TIMEOUT,
                AuditResult.CANCELLED,
                AuditResult.UNKNOWN
        }, values);
    }

    @Test
    public void testAuditResultGetValue() {
        assertEquals("success", AuditResult.SUCCESS.getValue());
        assertEquals("failure", AuditResult.FAILURE.getValue());
        assertEquals("denied", AuditResult.DENIED.getValue());
        assertEquals("invalid", AuditResult.INVALID.getValue());
        assertEquals("timeout", AuditResult.TIMEOUT.getValue());
        assertEquals("cancelled", AuditResult.CANCELLED.getValue());
        assertEquals("unknown", AuditResult.UNKNOWN.getValue());
    }

    @Test
    public void testAuditResultFromValue() {
        assertEquals(AuditResult.SUCCESS, AuditResult.fromValue("success"));
        assertEquals(AuditResult.FAILURE, AuditResult.fromValue("failure"));
        assertEquals(AuditResult.DENIED, AuditResult.fromValue("denied"));
        assertEquals(AuditResult.INVALID, AuditResult.fromValue("invalid"));
        assertEquals(AuditResult.TIMEOUT, AuditResult.fromValue("timeout"));
        assertEquals(AuditResult.CANCELLED, AuditResult.fromValue("cancelled"));
        assertEquals(AuditResult.UNKNOWN, AuditResult.fromValue("unknown"));
    }

    @Test
    public void testAuditResultFromValueCaseInsensitive() {
        assertEquals(AuditResult.SUCCESS, AuditResult.fromValue("SUCCESS"));
        assertEquals(AuditResult.FAILURE, AuditResult.fromValue("FAILURE"));
        assertEquals(AuditResult.DENIED, AuditResult.fromValue("DENIED"));
        assertEquals(AuditResult.INVALID, AuditResult.fromValue("INVALID"));
        assertEquals(AuditResult.TIMEOUT, AuditResult.fromValue("TIMEOUT"));
        assertEquals(AuditResult.CANCELLED, AuditResult.fromValue("CANCELLED"));
        assertEquals(AuditResult.UNKNOWN, AuditResult.fromValue("UNKNOWN"));
    }

    @Test
    public void testAuditResultFromValueNull() {
        assertNull(AuditResult.fromValue(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuditResultFromValueInvalid() {
        AuditResult.fromValue("invalid_result");
    }

    @Test
    public void testAuditResultJsonSerialization() throws Exception {
        String successJson = objectMapper.writeValueAsString(AuditResult.SUCCESS);
        String failureJson = objectMapper.writeValueAsString(AuditResult.FAILURE);
        String deniedJson = objectMapper.writeValueAsString(AuditResult.DENIED);
        String invalidJson = objectMapper.writeValueAsString(AuditResult.INVALID);
        String timeoutJson = objectMapper.writeValueAsString(AuditResult.TIMEOUT);
        String cancelledJson = objectMapper.writeValueAsString(AuditResult.CANCELLED);
        String unknownJson = objectMapper.writeValueAsString(AuditResult.UNKNOWN);

        assertEquals("\"success\"", successJson);
        assertEquals("\"failure\"", failureJson);
        assertEquals("\"denied\"", deniedJson);
        assertEquals("\"invalid\"", invalidJson);
        assertEquals("\"timeout\"", timeoutJson);
        assertEquals("\"cancelled\"", cancelledJson);
        assertEquals("\"unknown\"", unknownJson);
    }

    @Test
    public void testAuditResultJsonDeserialization() throws Exception {
        AuditResult success = objectMapper.readValue("\"success\"", AuditResult.class);
        AuditResult failure = objectMapper.readValue("\"failure\"", AuditResult.class);
        AuditResult denied = objectMapper.readValue("\"denied\"", AuditResult.class);
        AuditResult invalid = objectMapper.readValue("\"invalid\"", AuditResult.class);
        AuditResult timeout = objectMapper.readValue("\"timeout\"", AuditResult.class);
        AuditResult cancelled = objectMapper.readValue("\"cancelled\"", AuditResult.class);
        AuditResult unknown = objectMapper.readValue("\"unknown\"", AuditResult.class);

        assertEquals(AuditResult.SUCCESS, success);
        assertEquals(AuditResult.FAILURE, failure);
        assertEquals(AuditResult.DENIED, denied);
        assertEquals(AuditResult.INVALID, invalid);
        assertEquals(AuditResult.TIMEOUT, timeout);
        assertEquals(AuditResult.CANCELLED, cancelled);
        assertEquals(AuditResult.UNKNOWN, unknown);
    }

    @Test
    public void testAuditResultToString() {
        assertEquals("success", AuditResult.SUCCESS.toString());
        assertEquals("failure", AuditResult.FAILURE.toString());
        assertEquals("denied", AuditResult.DENIED.toString());
        assertEquals("invalid", AuditResult.INVALID.toString());
        assertEquals("timeout", AuditResult.TIMEOUT.toString());
        assertEquals("cancelled", AuditResult.CANCELLED.toString());
        assertEquals("unknown", AuditResult.UNKNOWN.toString());
    }

    @Test
    public void testAuditResultEquality() {
        AuditResult success1 = AuditResult.SUCCESS;
        AuditResult success2 = AuditResult.SUCCESS;
        AuditResult failure = AuditResult.FAILURE;

        assertEquals(success1, success2);
        assertNotEquals(success1, failure);
        assertSame(success1, success2); // Enums are singletons
    }

    @Test
    public void testAuditResultHashCode() {
        AuditResult success1 = AuditResult.SUCCESS;
        AuditResult success2 = AuditResult.SUCCESS;
        AuditResult failure = AuditResult.FAILURE;

        assertEquals(success1.hashCode(), success2.hashCode());
        assertNotEquals(success1.hashCode(), failure.hashCode());
    }

    @Test
    public void testAuditResultCompareTo() {
        assertTrue(AuditResult.SUCCESS.compareTo(AuditResult.FAILURE) < 0);
        assertTrue(AuditResult.FAILURE.compareTo(AuditResult.SUCCESS) > 0);
        assertEquals(0, AuditResult.SUCCESS.compareTo(AuditResult.SUCCESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuditResultValueOfInvalid() {
        AuditResult.valueOf("INVALID_RESULT");
    }

    @Test
    public void testAuditResultInSwitchStatement() {
        String result = switch (AuditResult.SUCCESS) {
            case SUCCESS -> "Operation completed successfully";
            case FAILURE -> "Operation failed";
            case DENIED -> "Operation was denied";
            case INVALID -> "Operation had invalid input";
            case TIMEOUT -> "Operation timed out";
            case CANCELLED -> "Operation was cancelled";
            case UNKNOWN -> "Operation result is unknown";
        };

        assertEquals("Operation completed successfully", result);
    }
} 