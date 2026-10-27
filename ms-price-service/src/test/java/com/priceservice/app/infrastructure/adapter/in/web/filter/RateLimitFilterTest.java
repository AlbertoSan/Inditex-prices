package com.priceservice.app.infrastructure.adapter.in.web.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    @Test
    void allowsRequestsWhileTokensAreAvailable() throws IOException, ServletException {
        RateLimitFilter filter = new RateLimitFilter(2, 0, 1);
        AtomicInteger requestsReachedEndpoint = new AtomicInteger();
        FilterChain chain = (request, response) -> requestsReachedEndpoint.incrementAndGet();

        filter.doFilter(request("192.0.2.1"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("192.0.2.1"), new MockHttpServletResponse(), chain);

        assertEquals(2, requestsReachedEndpoint.get());
    }

    @Test
    void returns429WhenTheBucketIsEmpty() throws IOException, ServletException {
        RateLimitFilter filter = new RateLimitFilter(1, 0, 1);
        AtomicInteger requestsReachedEndpoint = new AtomicInteger();
        FilterChain chain = (request, response) -> requestsReachedEndpoint.incrementAndGet();
        MockHttpServletRequest request = request("192.0.2.1");

        filter.doFilter(request, new MockHttpServletResponse(), chain);
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
        filter.doFilter(request, rejectedResponse, chain);

        assertEquals(429, rejectedResponse.getStatus());
        assertEquals("Too many requests", rejectedResponse.getContentAsString());
        assertEquals(1, requestsReachedEndpoint.get());
    }

    private MockHttpServletRequest request(String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        return request;
    }
}
