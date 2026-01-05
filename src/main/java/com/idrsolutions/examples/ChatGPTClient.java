/*
 * Copyright (C) 2026 IDRsolutions
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

import com.idrsolutions.image.JDeli;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import org.jpedal.utils.LogWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles a connection with ChatGPT.
 */
public class ChatGPTClient {

    private final OpenAIClient client;
    private final ChatModel model;

    /**
     * Create a new ChatGPTClient with your API key.
     *
     * @param apiKey your OpenAI API key
     */
    public ChatGPTClient(final ChatModel model, final String apiKey) {
        this.model = model;
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * Ask a question with context.
     *
     * @param question the question
     * @param textContext the text context
     * @param imageContext the image context
     * @return the response from ChatGPT
     */
    public Optional<String> ask(final String question, final String textContext, final BufferedImage imageContext) {
        final String prompt = """
                        The following text is extracted from a PDF the user is reading.
                        Use it to answer the user's question when relevant.
                        There is also an image attached which is the current page of the PDF.
                        
                        %s
                        
                        
                        User's question:
                        
                        %s
                        """.formatted(textContext, question);

        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JDeli.write(imageContext, "png", bos);
            final String data = "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());

            final ChatCompletionContentPart imagePart = ChatCompletionContentPart.ofImageUrl(
                    ChatCompletionContentPartImage.builder()
                            .imageUrl(
                                    ChatCompletionContentPartImage.ImageUrl.builder()
                                            .url(data)
                                            .build()
                            )
                            .build()
            );

            final ChatCompletionContentPart textPart = ChatCompletionContentPart.ofText(
                    ChatCompletionContentPartText.builder()
                            .text(prompt)
                            .build()
            );

            final ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .addUserMessageOfArrayOfContentParts(List.of(textPart, imagePart))
                    .build();

            final ChatCompletion completion = client.chat().completions().create(params);

            return completion.choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .map(Objects::toString)
                    .filter(s -> s != null && !s.isBlank())
                    .findFirst();
        } catch (final Exception e) {
            LogWriter.warning(e, "Error: " + e.getMessage());
            return Optional.empty();
        }
    }

}
