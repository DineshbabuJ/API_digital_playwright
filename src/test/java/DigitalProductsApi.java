import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DigitalProductsApi {
    private Playwright playWright;
    private APIRequestContext request;

    public DigitalProductsApi() throws FileNotFoundException {
    }

    void createPlayWright(){
        playWright=Playwright.create();
    }
    int brandID;
    String productTypeCode;
    String cartID;
    String defaultProviderId;
    String defaultProviderName;
    String skuCode;
    String paymentMethod;
    String orderId;
    String brandName;
    String deviceid;
    String INTERNAL_NAME;
    FileReader reader=new FileReader("application.properties");
    Properties properties= new Properties();

    void createAPIRequestContext(String url) throws IOException {
        properties.load(reader);
        productTypeCode= properties.getProperty("productTypeCode");
        brandName=properties.getProperty("brandname");
        deviceid=properties.getProperty("deviceid");
        INTERNAL_NAME=properties.getProperty("internalname");
        cartID=properties.getProperty("cartID");
        Map<String,String> headers=new HashMap<>();
        headers.put("accept","application/json");
        headers.put("Content-Type","application/json");
        request=playWright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(url).setExtraHTTPHeaders(headers));
    }

    @Test(priority = 1)
    public void getItemMapping() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://digital-catalog.qa2-sg.cld/digital-catalog/api/");
        APIResponse response=request
                .get("itemMapping/findAll", RequestOptions.create()
                        .setQueryParam("storeId",10001)
                        .setQueryParam("requestId","RANDOM")
                        .setQueryParam("channelId","pulsa-web")
                        .setQueryParam("clientId","pulsa")
                        .setQueryParam("username","username"));
        JSONObject jsonResponse = new JSONObject(response.text());
        JSONArray contentArray = jsonResponse.getJSONArray("content");
        for (int i = 0; i < contentArray.length(); i++) {
            JSONObject item = contentArray.getJSONObject(i);
            JSONObject brand = item.getJSONObject("brand");
            JSONObject productType = item.getJSONObject("productType");
            if (productTypeCode.equals(productType.getString("productTypeCode")) &&
                    brandName.equals(brand.getString("brandName"))) {
                System.out.println("brandId: " + brand.getInt("brandId"));
                brandID=brand.getInt("brandId");
            }
        }
        Assert.assertEquals(200,response.status());
        Assert.assertTrue(response.text().contains("\"success\":true"), "Response should indicate success");
    }

    @Test(priority = 2)
    public void GetDefaultProviderId() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://digital-catalog.qa2-sg.cld/digital-catalog/api/");
        APIResponse response=request
                .get("itemMapping/findOne", RequestOptions.create()
                        .setQueryParam("storeId",10001)
                        .setQueryParam("requestId","RANDOM")
                        .setQueryParam("channelId","pulsa-web")
                        .setQueryParam("clientId","pulsa")
                        .setQueryParam("username","username")
                        .setQueryParam("brandId",brandID)
                        .setQueryParam("productTypeCode","PHONE_CREDIT"));
        System.out.println(response.text());
        JSONObject jsonResponse = new JSONObject(response.text());
        JSONObject value = jsonResponse.getJSONObject("value");
        defaultProviderId = value.getString("defaultProviderId");
        defaultProviderName = value.getString("defaultProviderName");
        System.out.println("Default Provider Id :"+defaultProviderId);
        System.out.println("Default Provider Name :"+defaultProviderName);
        Assert.assertEquals(200,response.status());
        Assert.assertTrue(response.text().contains("\"success\":true"), "Response should indicate success");
    }

    @Test(priority = 3)
    public void SaveItemMapping() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://digital-catalog.qa2-sg.cld/digital-catalog/api/");
        JSONObject requestBody = new JSONObject();
        requestBody.put("active", true);
        requestBody.put("autoSwitch", "false");
        requestBody.put("brandId", brandID);
        requestBody.put("defaultProviderId",defaultProviderId);
        requestBody.put("defaultProviderName",defaultProviderName);
        requestBody.put("msisdn", "0814,0815,0816,0855,0856,0857,0858");
        requestBody.put("productTypeCode",productTypeCode);

        APIResponse response=request
                .post("itemMapping/save", RequestOptions.create().setQueryParam("storeId", 10001)
                .setQueryParam("requestId", "RANDOM")
                .setQueryParam("channelId", "pulsa-web")
                .setQueryParam("clientId", "pulsa")
                .setQueryParam("username", "username")
                .setData(requestBody.toString()));

        Assert.assertEquals(response.status(), 200, "Expected status code 200 OK");
        String jsonResponse = response.text();
        System.out.println("-------save Item Mapping------- \n"+jsonResponse);
        Assert.assertTrue(jsonResponse.contains("\"requestId\":\"RANDOM\""), "Response should contain requestId");
        Assert.assertTrue(jsonResponse.contains("\"success\":true"), "Response should indicate success");
        Assert.assertTrue(jsonResponse.contains("\"defaultProviderId\":\"BLP-25978\""), "Response should contain defaultProviderId");
        Assert.assertTrue(jsonResponse.contains("\"defaultProviderName\":\"NARINDO\""), "Response should contain defaultProviderName");
        Assert.assertTrue(jsonResponse.contains("\"productTypeCode\":\"PHONE_CREDIT\""), "Response should contain productTypeCode");

    }


    @Test(priority = 4)
    public void deleteExistingCart() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response=request.
                delete("pulsaCart/deletePulsaCart",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setQueryParam("cartId",cartID));
        System.out.println(response.text());
        Assert.assertEquals(response.status(), 200, "Expected status code 200 OK");
    }

    @Test(priority = 5)
    public void getSkuCode() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://digital-catalog.qa2-sg.cld/digital-catalog/api/");

        APIResponse response=request
                .get("merchantDigitalSku/getProductListByFilter", RequestOptions.create().
                        setQueryParam("storeId",10001).
                        setQueryParam("requestId","RANDOM").
                        setQueryParam("channelId","pulsa-web").
                        setQueryParam("clientId","pulsa").
                        setQueryParam("username","username").
                        setQueryParam("productType",productTypeCode).
                        setQueryParam("brandName",brandName));
        JSONObject jsonResponse = new JSONObject(response.text());
        JSONArray contentArray = jsonResponse.getJSONArray("content");
        for (int i = 0; i < contentArray.length(); i++) {
            JSONObject product = contentArray.getJSONObject(i);
            JSONObject digitalProduct = product.getJSONObject("digitalProduct");
            String internalName = digitalProduct.optString("internalName");
            if (INTERNAL_NAME.equals(internalName)) {
                JSONArray merchantSkuList = product.getJSONArray("merchantDigitalSkuList");
                for (int j = 0; j < merchantSkuList.length(); j++) {
                    JSONObject merchantSku = merchantSkuList.getJSONObject(j);
                    String merchantName = merchantSku.optString("merchantName");
                    if (defaultProviderName.equals(merchantName)) {
                        skuCode = merchantSku.optString("skuCode");
                        break;
                    }
                }
                if (skuCode != null) {
                    break;
                }
            }
        }
        System.out.println(skuCode);
        Assert.assertEquals(200,response.status());
    }

    @Test(priority = 6)
    public void doCheckOut() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
         APIResponse response= request
                 .post("pulsaCart/doCheckout",RequestOptions.create()
                         .setQueryParam("storeId", 10001)
                         .setQueryParam("requestId", "RANDOM")
                         .setQueryParam("channelId", "pulsa-web")
                         .setQueryParam("clientId", "pulsa")
                         .setQueryParam("username", "username")
                         .setQueryParam("cartId",cartID)
                         .setQueryParam("customerLogonId",cartID)
                         .setQueryParam("cartOwnershipType","MEMBER"));
        System.out.println(response.text());
        Assert.assertEquals(response.status(), 200, "Expected status code 200 OK");
    }

    @Test(priority = 7)
    public void setPulsaCart() throws IOException {
        HashMap<String,Object> reqBody= new HashMap<>();
        reqBody.put("itemSku",skuCode);
        reqBody.put("operatorCode",brandName);
        reqBody.put("providerName",defaultProviderName);
        System.out.println(reqBody.toString());
        System.out.println(cartID);
        System.out.println(productTypeCode);
        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .post("pulsaCart/setPulsaCartMsisdn",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setQueryParam("cartId",cartID)
                        .setQueryParam("msisdn","085765160652")
                        .setQueryParam("productType",productTypeCode)
                        .setData(reqBody));
        System.out.println(response.text());
        Assert.assertEquals(200,response.status());
    }

    @Test(priority =8)
    public void addToCart() throws IOException {
        HashMap<String,Object> reqBody= new HashMap<>();
        reqBody.put("productType",productTypeCode);
        reqBody.put("itemSku",skuCode);
        reqBody.put("cartId",cartID);
        reqBody.put("operatorName",brandName);
        reqBody.put("deviceId",deviceid);

        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .post("pulsaCart/addToCart",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setData(reqBody));

        System.out.println(response.text());
        Assert.assertEquals(200,response.status());
    }

    @Test(priority = 9)
    public void changePaymentMethod() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .post("pulsaCart/changePayment",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setQueryParam("cartId",cartID)
                        .setQueryParam("paymentMethod",paymentMethod));
        Assert.assertEquals(200,response.status());
        System.out.println(response.text());
    }

    @Test(priority = 10)
    public void payOrder() throws IOException {
        HashMap<String,Object> reqBody= new HashMap<>();
        reqBody.put("pulsaCartId",cartID);
        reqBody.put("extendedData", new HashMap<String, Object>());

        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .post("pulsaCart/payOrder",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setData(reqBody));
        System.out.println(response.text());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.text());
        JsonNode valueNode = rootNode.path("value");
        orderId = valueNode.path("orderId").asText();
        System.out.println(orderId);
        Assert.assertEquals(200,response.status());
    }

    @Test(priority = 11)
    public void approveOrder() throws IOException {
        HashMap<String,Object> reqBody= new HashMap<>();
        reqBody.put("orderId",orderId);
        reqBody.put("extData",new HashMap<String, Object>());

        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .post("approveOrder/approveOrderPayment",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setData(reqBody));
        Assert.assertEquals(200,response.status());
        System.out.println(response.text());
    }

    @Test(priority = 12)
    public void getOrderVerify() throws IOException {
        createPlayWright();
        createAPIRequestContext("http://x-pulsa.qa2-sg.cld/x-pulsa/api/");
        APIResponse response= request
                .get("pulsaOrder/getPulsaOrderByOrderId",RequestOptions.create()
                        .setQueryParam("storeId", 10001)
                        .setQueryParam("requestId", "RANDOM")
                        .setQueryParam("channelId", "pulsa-web")
                        .setQueryParam("clientId", "pulsa")
                        .setQueryParam("username", "username")
                        .setQueryParam("orderId",orderId));
        System.out.println(response.text());
        String responseBody = response.text();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonObject pulsaCartItem = jsonResponse
                .getAsJsonObject("value")
                .getAsJsonObject("pulsaOrder")
                .getAsJsonObject("pulsaCartItem");
        String providerName = pulsaCartItem.get("providerName").getAsString();
        String description = pulsaCartItem.get("description").getAsString();
        Assert.assertEquals(providerName,defaultProviderName);
        Assert.assertEquals(description,INTERNAL_NAME);
        Assert.assertEquals(200,response.status());
    }
}
