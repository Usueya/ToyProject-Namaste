package org.spring.my.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spring.my.dao.YmemberDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NaverLoginServiceImpl implements NaverLoginService{
	
	@Autowired
	private YmemberDAO yMemberDAO;

	@Override
	public Map<String, String> getApiUrl() throws Exception {
	    String clientId = "INzteden2Y6ovxOn9TMM"; //애플리케이션 클라이언트 아이디값
	    String redirectURI = URLEncoder.encode("http://localhost:8081/my/naverlogin/callback", "UTF-8"); //콜백주소
	    SecureRandom random = new SecureRandom();
	    String state = new BigInteger(130, random).toString();
	    String apiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code";
	    apiURL += "&client_id=" + clientId;
	    apiURL += "&redirect_uri=" + redirectURI;
	    apiURL += "&state=" + state;
	    Map<String, String> resultMap = new HashMap<String, String>();
	    resultMap.put("apiURL", apiURL);
	    resultMap.put("state", state);
		return resultMap;
	}

	//(접근할 수 있는)토큰 발급
	@Override
	public String getToken(String code, String state) throws Exception{
	    String clientId = "INzteden2Y6ovxOn9TMM";//애플리케이션 클라이언트 아이디값";
	    String clientSecret = "iMGyYdOu7n";//애플리케이션 클라이언트 시크릿값";
	    //String code = request.getParameter("code");
	   // String state = request.getParameter("state");
	    String redirectURI = URLEncoder.encode("http://localhost:8081/my/naverlogin/callback", "UTF-8");
	    String apiURL;
	    apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
	    apiURL += "client_id=" + clientId;
	    apiURL += "&client_secret=" + clientSecret;
	    apiURL += "&redirect_uri=" + redirectURI;
	    apiURL += "&code=" + code;
	    apiURL += "&state=" + state;
	    String access_token = "";
	    String refresh_token = "";
	    System.out.println("apiURL="+apiURL);
	    try {
	      URL url = new URL(apiURL);
	      HttpURLConnection con = (HttpURLConnection)url.openConnection();
	      con.setRequestMethod("GET");
	      int responseCode = con.getResponseCode();
	      BufferedReader br;
	      System.out.print("responseCode="+responseCode);
	      if(responseCode==200) { // 정상 호출
	        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
	      } else {  // 에러 발생
	        br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
	      }
	      String inputLine;
	      StringBuffer res = new StringBuffer();
	      while ((inputLine = br.readLine()) != null) {
	        res.append(inputLine);
	      }
	      br.close();
	      if(responseCode==200) {
	    	System.out.println(res.toString()); //리턴받은값{오브젝트}을출력
	    	
	    	//json반환값 파싱하기(라이브러리 유무확인 JSON.simple)
	    	JSONObject object = (JSONObject) new JSONParser().parse(res.toString());
	    	
	    	access_token = (String) object.get("access_token");
	    	refresh_token = (String) object.get("refresh_token");

	      }
	    } catch (Exception e) {
	      System.out.println(e);
	    }
	    
		return access_token;
	}
	
	//회원프로필조회
	@Override
    public Map<String, String> getNaverUserInfo(String access_token) throws Exception{
		Map<String, String> resultMap = new HashMap<>();
        String token = access_token; // 네이버 로그인 접근 토큰;
        String header = "Bearer " + token; // Bearer 다음에 공백 추가

        String apiURL = "https://openapi.naver.com/v1/nid/me";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        String responseBody = get(apiURL,requestHeaders);

        System.out.println(responseBody);
        
        //json파싱
        JSONObject object = (JSONObject) new JSONParser().parse(responseBody);
        object = (JSONObject) object.get("response");
        String id = (String) object.get("id");
        String email = (String) object.get("email");
        String name = (String) object.get("name");
        
        resultMap.put("id", id);
        resultMap.put("email", email);
        resultMap.put("name", name);
        
        return resultMap;
        
    }

    private String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);
        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

	@Override
	public void insert(Map<String, String> resultMap) {
		//회원가입유무
		String id = resultMap.get("id");
		if(yMemberDAO.selectOne(id) == null) {
			yMemberDAO.insertNaverLogin(resultMap);
		}
	}

}
