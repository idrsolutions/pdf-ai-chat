# Chat to an LLM about the PDF file you are currently viewing

A proof of concept which adds a chat window to the [JPedal Viewer](https://www.idrsolutions.com/jpedal/) and allows the
user to have a conversation with [ChatGPT](https://github.com/openai/openai-java).It includes the current text and an
image of the page as context so you can ask it questions about the PDF file.

This project demonstrates how easy it is to extend the JPedal Viewer with custom components that interact with the
currently loaded document.

You will need to [create an OpenAI API key](https://platform.openai.com/api-keys) to use their models. Currently, the
project uses `GPT-5 nano` as this is the cheapest, however you can easily switch to a more powerful model.