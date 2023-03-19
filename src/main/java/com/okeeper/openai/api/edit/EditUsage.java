package com.okeeper.openai.api.edit;

import lombok.Data;

/**
 * An object containing the API usage for an edit request
 *
 * Deprecated, use {@link com.okeeper.openai.api.Usage} instead
 *
 * https://beta.openai.com/docs/api-reference/edits/create
 */
@Data
@Deprecated
public class EditUsage {

    /**
     * The number of prompt tokens consumed.
     */
    String promptTokens;

    /**
     * The number of completion tokens consumed.
     */
    String completionTokens;

    /**
     * The number of total tokens consumed.
     */
    String totalTokens;
}
