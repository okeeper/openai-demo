package com.okeeper;

import com.okeeper.openai.OpenAiService;
import com.okeeper.openai.api.completion.CompletionRequest;

public class Demo {
    public static void main(String... args) {
        //String token = System.getenv("OPENAI_TOKEN");
        String token = "**********";
        OpenAiService service = new OpenAiService(token);

        System.out.println("\nCreating completion...");
        try{
            CompletionRequest completionRequest = CompletionRequest.builder()
                    .model("text-davinci-001")
                    .prompt("写一封中文请假条，请假原因是去医院看病，要求字数不少于200")
                    .echo(false)
                    .user("testingzhangyue0808")
                    .maxTokens(500)
                    .n(1)
                    .build();
            service.createCompletion(completionRequest).getChoices().forEach(System.out::println);
        }catch (Throwable e) {
            e.printStackTrace();
        }


//        System.out.println("\nCreating Image...");
//        CreateImageRequest request = CreateImageRequest.builder()
//                .prompt("画一只仰望天空的牛")
//                .build();
//
//        System.out.println("\nImage is located at:");
//        System.out.println(service.createImage(request).getData().get(0).getUrl());
    }
}
