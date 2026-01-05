package com.dsa.summarizer.service;

import com.dsa.summarizer.model.DSASummary;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final Gson gson = new Gson();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private static final String[] PRIORITY_KEYWORDS = {
            "dp","dynamic programming","O(","binary search","greedy",
            "optimize","optimization","edge case","complexity",
            "time complexity","space complexity","algorithm","approach","solution"
    };

    public DSASummary generateDSASummaryFromMergedChunks(List<String> chunks, String videoId) throws Exception {

        List<String> selected = selectTopChunks(chunks,4);
        String merged = String.join("\n\n", selected);
        String prompt = buildSingleCallPrompt(merged);

        String raw = callGeminiAPI(prompt);
        return parseStructuredResponse(raw, videoId);
    }

    private List<String> selectTopChunks(List<String> chunks,int max){
        if(chunks.size()<=max) return chunks;

        List<ChunkScore> scores=new ArrayList<>();
        for(String c:chunks){
            int score=0;
            String l=c.toLowerCase();
            for(String k:PRIORITY_KEYWORDS)
                if(l.contains(k)) score++;
            scores.add(new ChunkScore(c,score));
        }

        scores.sort((a,b)->b.score-a.score);
        List<String> out=new ArrayList<>();
        for(int i=0;i<max;i++) out.add(scores.get(i).text);
        return out;
    }

    private String buildSingleCallPrompt(String content){

        return """
    Generate DSA revision notes.

    STRICT RULES:
    - You MUST use the tags exactly as written.
    - Do NOT rename, omit, or reorder tags.
    - Do NOT use markdown or bullet symbols.

    FORMAT:

    [PROBLEM]
    <one paragraph>
    [/PROBLEM]

    [ALGORITHM]
    1.
    2.
    3.
    4.
    [/ALGORITHM]

    [PSEUDOCODE]
    <plain text>
    [/PSEUDOCODE]

    [TIME]
    <Big-O>
    [/TIME]

    [SPACE]
    <Big-O>
    [/SPACE]

    [EDGE]
    1.
    2.
    3.
    [/EDGE]

    [REVISION]
    1.
    2.
    3.
    4.
    5.
    [/REVISION]

    CONTENT:
    """ + content;
    }


    private String callGeminiAPI(String prompt) throws Exception{

        URL url=new URL(apiUrl+"?key="+apiKey);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setDoOutput(true);

        JsonObject req=new JsonObject();
        JsonArray contents=new JsonArray();
        JsonObject content=new JsonObject();
        JsonArray parts=new JsonArray();
        JsonObject part=new JsonObject();
        part.addProperty("text",prompt);
        parts.add(part);
        content.add("parts",parts);
        contents.add(content);
        req.add("contents",contents);

        try(OutputStream os=conn.getOutputStream()){
            os.write(gson.toJson(req).getBytes(StandardCharsets.UTF_8));
        }

        BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder res=new StringBuilder();
        String line;
        while((line=br.readLine())!=null) res.append(line);

        JsonObject json=JsonParser.parseString(res.toString()).getAsJsonObject();
        return clean(json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString());
    }

    private String clean(String t){
        return t.replaceAll("(?m)^#+","")
                .replace("**","")
                .replace("*","")
                .replaceAll("```[a-zA-Z]*","")
                .replace("`","")
                .replaceAll("\\n{3,}","\n\n").trim();
    }

    private DSASummary parseStructuredResponse(String t,String id){
        DSASummary s=new DSASummary();
        s.setVideoId(id);
        s.setProblemSummary(extract(t,"PROBLEM"));
        s.setAlgorithmSteps(extract(t,"ALGORITHM"));
        s.setPseudocode(extract(t,"PSEUDOCODE"));
        s.setTimeComplexity(extract(t,"TIME"));
        s.setSpaceComplexity(extract(t,"SPACE"));
        s.setEdgeCases(extract(t,"EDGE"));
        s.setRevisionNotes(extract(t,"REVISION"));
        return s;
    }

    private String extract(String text, String tag) {

        String open = "[" + tag + "]";
        String close = "[/" + tag + "]";

        int start = text.indexOf(open);
        int end = text.indexOf(close);

        // 1️⃣ Perfect case
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start + open.length(), end).trim();
        }

        // 2️⃣ Fallback to HEADER style
        String altOpen = tag + ":";
        start = text.toUpperCase().indexOf(altOpen);
        if (start == -1) return "Not available";

        start += altOpen.length();
        end = text.length();

        String[] tags = {"PROBLEM","ALGORITHM","PSEUDOCODE","TIME","SPACE","EDGE","REVISION"};
        for (String t : tags) {
            if (!t.equals(tag)) {
                int pos = text.toUpperCase().indexOf(t + ":", start);
                if (pos != -1 && pos < end) end = pos;
            }
        }

        return text.substring(start, end).trim();
    }


    private static class ChunkScore{
        String text; int score;
        ChunkScore(String t,int s){text=t;score=s;}
    }
}
