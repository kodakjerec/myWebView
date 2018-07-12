# WebView plugin for special bank #
提供給特定公司的webview plugin

## 特色 ##
1. 每一次touch都會回傳點選時間
- 回傳格式：{"type":"touch","lastTouchTime": @millsecond@ }
`@millsecond@ 是使用者按下時機的絕對時間`
<pre>InvestmentWebView.addEventListener('touch', successCallback, errorCallback)
function successCallback(object){
    console.log(object.lastTouchTime) // 1534412
}
function errorCallback(object){
    console.log(object) // android/ios error message
}
</pre>

2. 攔截網頁中的button click, 並回傳html ID
- 回傳格式：{"type":"buttonclick","id": @buttonid@ }
`@buttonid@ 是使用者按下的button id`
<pre>InvestmentWebView.addEventListener('buttonclick', successCallback, errorCallback)
function successCallback(object){
    console.log(object.id) // CloseBtn009
}
function errorCallback(object){
    console.log(object) // android/ios error message
}
</pre>

3. 切換網頁都會回傳網址
- 回傳格式：{"type":"loadstart","url": @url@ }
`@url@ 是切換後的目的網址`
<pre>InvestmentWebView.addEventListener('loadstart', successCallback, errorCallback)
function successCallback(object){
    console.log(object.url) // https://www.google.com
}
function errorCallback(object){
    console.log(object) // android/ios error message
}
</pre>

4. 支援從cordova上層直接關閉webview
<pre>InvestmentWebView.close()</pre>

5. 開啟網址
- `title(string): 手機上webview標題文字`
- `url(string): 網址`
- `type(int): 特定說明文件`
- `noteButtonString(string): 說明按鈕要顯示給用戶的文字`
<pre>InvestmentWebView.openWeb(title, url, type, noteButtonString)</pre>
