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

import org.jpedal.PdfDecoderInt;
import org.jpedal.examples.viewer.Viewer;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.LogWriter;

import javax.swing.JTabbedPane;
import java.util.Optional;

public class PdfAiChat {

    public static void main(String[] args) {
        // Set up and open the JPedal Viewer
        final Viewer viewer = new Viewer();
        viewer.setupViewer();

        // Create chat window and ChatGPTClient
        final ChatPanel chatPanel = new ChatPanel();
        final ChatGPTClient gpt = new ChatGPTClient("XXXXXX");

        chatPanel.setSendHandler(userMessage -> new Thread(() -> {
            try {
                // Get the text from the current page
                final PdfDecoderInt pdfDecoder = viewer.getPdfDecoder();
                final int pageNum = viewer.getSwingGUI().getValues().getCurrentPage();
                final PdfPageData pageData = pdfDecoder.getPdfPageData();
                final String pageText = pdfDecoder.getGroupingObject().extractTextInRectangle(
                        pageData.getMediaBoxX(pageNum),
                        pageData.getMediaBoxY(pageNum),
                        pageData.getMediaBoxWidth(pageNum),
                        pageData.getMediaBoxHeight(pageNum),
                        pageNum,
                        false, false);

                // Pass the text and question to the AI
                final Optional<String> response = gpt.ask(pageText, userMessage);
                chatPanel.addMessage(response.orElse(""), false);
            } catch (Exception ex) {
                chatPanel.addMessage("Error: " + ex.getMessage(), false);
                LogWriter.warning(ex, "Error: " + ex.getMessage());
            }
        }).start());

        // Add to JPedal panels
        final JTabbedPane sideTabBar = viewer.getSwingGUI().getSideTabBar();
        sideTabBar.addTab("AI", new ImageIcon(), chatPanel);
    }

}
