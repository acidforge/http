# AcidForge Http Client
*The fastest, easiest and trustworthy HttpClient library*

The client was built using core JVM interfaces for http pipeline requests, supportting SSL and Cookies for almost anything you want to send between a webservice and an Android App.
The only dependency for this project is Guava *https://github.com/google/guava*

Getting started:
Checkout repos under your project and mark as library
Set your apk permissions to use internet

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

The library works with almost every mimetype supported by http pipeline, for that you must only override the methods requestStream and responseStream.


## Common Usage for Get as Json
By the use of jackson library, to get a response as json is simply as follows.
```
Context context = MyActivity.this;
HttpRequest httpRequest = new HttpRequest(context, new new URL("http://10.0.2.2:4005"){
	@Override
	public void responseStream(InputStream stream) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Account account = objectMapper.readerFor(Account.class).readValue(stream);
	}
};
httpRequest.execute();
```

##Common Usage for Post as Json
```
Context context = MyActivity.this;
HttpRequest httpRequest = new HttpRequest(context, new new URL("http://10.0.2.2:4005"){
	@Override
	public void requestStream(OutputStream stream) throws Exception {
		Account account = this.getAccount();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writer().writeValue(stream, model);
	}
	
	@Override
	public void responseStream(InputStream stream) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Account account = objectMapper.readerFor(Account.class).readValue(stream);
	}
};
httpRequest.execute();
```

##There are a lot of usages, you can do with the HttpRequest
You can use the stream to write down images, text files, upload files.
You'll just need to setup the mimetype for the underlying connection.
We use Guava library for that.
After that send bytes over the writeStream method and catch the response to deal successes or failures.
```
HttpRequest httpRequest = new HttpRequest(context, new new URL("http://10.0.2.2:4005"){
	//...common overrides
	};
	
httprequest.setRequestMediaType(MediaType.AAC_AUDIO);
```
