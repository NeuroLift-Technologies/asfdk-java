package com.neurolift.asfdk.integration;

/**
 * Identity of a TOI document.
 *
 * @param author the document author
 */
public record ToiIdentity(
        String author
) {
    public ToiIdentity {
        author = author == null ? "anonymous" : author;
    }

    public ToiIdentity() {
        this("anonymous");
    }
}