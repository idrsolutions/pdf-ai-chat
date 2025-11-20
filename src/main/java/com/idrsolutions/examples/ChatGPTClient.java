/*
 * Copyright (C) 2025 IDRsolutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.idrsolutions.examples;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.Optional;

/**
 * Handles a connection with ChatGPT.
 */
public class ChatGPTClient {

    private final OpenAIClient client;

    /**
     * Create a new ChatGPTClient with your API key.
     *
     * @param apiKey your OpenAI API key
     */
    public ChatGPTClient(String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Ask GPT-4.1 mini a question with context.
     *
     * @param question the question
     * @param context the content
     * @return the response from ChatGPT
     */
    public Optional<String> ask(final String question, final String context) {
        final String prompt = """
                        The following text is extracted from a PDF the user is reading.
                        Use it to answer the user's question when relevant.
                        
                        %s
                        
                        
                        User's question:
                        
                        %s
                        """;

        final ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addUserMessage(prompt.formatted(context, question))
                .build();

        final ChatCompletion completion = client.chat().completions().create(params);

        return completion.choices().get(0).message().content();
    }

}
