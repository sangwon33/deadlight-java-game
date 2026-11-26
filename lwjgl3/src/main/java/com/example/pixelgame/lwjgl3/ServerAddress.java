package com.example.pixelgame.lwjgl3;

import com.badlogic.gdx.utils.JsonReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/** Pure address validation and a read-only check before starting the OpenGL game. */
final class ServerAddress {
    private ServerAddress() {}

    static String normalize(String input) {
        String text=input==null?"":input.trim().replaceAll("/+$","");
        if(text.isEmpty()||text.length()>2048) throw new IllegalArgumentException("서버 주소를 입력해 주세요.");
        try {
            URI uri=URI.create(text);
            String scheme=uri.getScheme()==null?"":uri.getScheme().toLowerCase(Locale.ROOT);
            if(!(scheme.equals("http")||scheme.equals("https"))||uri.getHost()==null
                    ||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null
                    ||uri.getPort()==0||uri.getPort()>65535
                    ||!(uri.getPath().isEmpty()||uri.getPath().equals("/"))) throw new IllegalArgumentException();
            return new URI(scheme,null,uri.getHost(),uri.getPort(),null,null,null).toASCIIString();
        } catch(Exception e) {
            throw new IllegalArgumentException("서버 담당자에게 받은 https:// 주소를 그대로 입력해 주세요.");
        }
    }

    static void check(String baseUrl) throws IOException,InterruptedException {
        var client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var request=HttpRequest.newBuilder(URI.create(normalize(baseUrl)+"/api/health"))
                .timeout(Duration.ofSeconds(8)).header("Accept","application/json").GET().build();
        var response=client.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200) throw new IOException("서버가 연결을 확인하지 못했습니다. (HTTP "+response.statusCode()+")");
        try {
            var json=new JsonReader().parse(response.body());
            if(!json.isObject()||!"ok".equals(json.getString("status",""))||!"deadlight-records".equals(json.getString("service","")))
                throw new IllegalArgumentException();
        } catch(RuntimeException e) {
            throw new IOException("Deadlight 기록 서버를 확인할 수 없습니다. 서버 담당자에게 새 주소를 확인해 주세요.");
        }
    }
}
