package com.enterprise.audit.logging.exception;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuditLoggingExceptionTest {

    @Test
    public void testDefaultConstructor() {
        AuditLoggingException exception = new AuditLoggingException();
        
        assertNull("Message should be null", exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithMessage() {
        String message = "Test audit logging exception";
        AuditLoggingException exception = new AuditLoggingException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithNullMessage() {
        AuditLoggingException exception = new AuditLoggingException((String) null);
        
        assertNull("Message should be null", exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithEmptyMessage() {
        String message = "";
        AuditLoggingException exception = new AuditLoggingException(message);
        
        assertEquals("Message should be empty string", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "Test audit logging exception with cause";
        Throwable cause = new RuntimeException("Root cause");
        AuditLoggingException exception = new AuditLoggingException(message, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
    }

    @Test
    public void testConstructorWithNullMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        AuditLoggingException exception = new AuditLoggingException((String) null, cause);
        
        assertNull("Message should be null", exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
    }

    @Test
    public void testConstructorWithMessageAndNullCause() {
        String message = "Test audit logging exception with null cause";
        AuditLoggingException exception = new AuditLoggingException(message, null);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithNullMessageAndNullCause() {
        AuditLoggingException exception = new AuditLoggingException((String) null, null);
        
        assertNull("Message should be null", exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        AuditLoggingException exception = new AuditLoggingException(cause);
        
        assertNotNull("Message should not be null", exception.getMessage());
        assertTrue("Message should contain cause message", exception.getMessage().contains("Root cause"));
        assertEquals("Cause should match", cause, exception.getCause());
    }

    @Test
    public void testConstructorWithNullCause() {
        AuditLoggingException exception = new AuditLoggingException((Throwable) null);
        
        assertNull("Message should be null", exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
    }

    @Test
    public void testExceptionInheritance() {
        AuditLoggingException exception = new AuditLoggingException("Test");
        
        assertTrue("Should be instance of Exception", exception instanceof Exception);
        assertTrue("Should be instance of AuditLoggingException", exception instanceof AuditLoggingException);
    }

    @Test
    public void testSerialVersionUID() {
        // Test that the serialVersionUID is accessible (though it's private)
        // This is more of a compilation test to ensure the field exists
        AuditLoggingException exception = new AuditLoggingException();
        
        // The exception should be serializable
        assertTrue("Exception should be serializable", exception instanceof java.io.Serializable);
    }

    @Test
    public void testExceptionChaining() {
        // Create a chain of exceptions
        RuntimeException rootCause = new RuntimeException("Root cause");
        AuditLoggingException middleException = new AuditLoggingException("Middle exception", rootCause);
        AuditLoggingException topException = new AuditLoggingException("Top exception", middleException);
        
        assertEquals("Top exception cause should be middle exception", middleException, topException.getCause());
        assertEquals("Middle exception cause should be root cause", rootCause, middleException.getCause());
        assertEquals("Root cause should be the original exception", rootCause, topException.getCause().getCause());
    }

    @Test
    public void testExceptionStackTrace() {
        AuditLoggingException exception = new AuditLoggingException("Test exception");
        
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull("StackTrace should not be null", stackTrace);
        assertTrue("StackTrace should have elements", stackTrace.length > 0);
        
        // The first element should be this test method
        StackTraceElement firstElement = stackTrace[0];
        assertTrue("First stack trace element should be this test method", 
                firstElement.getMethodName().contains("testExceptionStackTrace"));
    }

    @Test
    public void testExceptionWithComplexCause() {
        // Create a complex exception chain
        NullPointerException nullPtr = new NullPointerException("Null pointer");
        RuntimeException runtime = new RuntimeException("Runtime error", nullPtr);
        AuditLoggingException auditException = new AuditLoggingException("Audit failed", runtime);
        
        assertEquals("Direct cause should be runtime exception", runtime, auditException.getCause());
        assertEquals("Root cause should be null pointer exception", nullPtr, auditException.getCause().getCause());
        // Note: The chain is: auditException -> runtime -> nullPtr
        // There's no direct link from nullPtr to illegalArg in this chain
        assertNull("No direct link to illegal argument in this chain", auditException.getCause().getCause().getCause());
    }

    @Test
    public void testExceptionMessageFormatting() {
        String message = "Audit logging failed for user: %s, action: %s";
        String formattedMessage = String.format(message, "user123", "login");
        AuditLoggingException exception = new AuditLoggingException(formattedMessage);
        
        assertEquals("Message should be formatted correctly", formattedMessage, exception.getMessage());
        assertTrue("Message should contain user ID", exception.getMessage().contains("user123"));
        assertTrue("Message should contain action", exception.getMessage().contains("login"));
    }

    @Test
    public void testExceptionWithSpecialCharacters() {
        String message = "Audit exception with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        AuditLoggingException exception = new AuditLoggingException(message);
        
        assertEquals("Message should preserve special characters", message, exception.getMessage());
    }

    @Test
    public void testExceptionWithUnicodeCharacters() {
        String message = "Audit exception with unicode: 测试审计日志异常 🚀";
        AuditLoggingException exception = new AuditLoggingException(message);
        
        assertEquals("Message should preserve unicode characters", message, exception.getMessage());
    }
} 