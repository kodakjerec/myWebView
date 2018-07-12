# WebView plugin for special bank #
提供給特定公司的webview plugin

## 特色 ##
1. 每一次touch都會回傳點選時間
- InvestmentWebView.addEventListener('touch', successCallback, errorCallback)
- {"type":"touch","lastTouchTime": @millsecond@ }
@millsecond@ 是使用者按下時機的絕對時間

2. 攔截網頁中的button click, 並回傳html ID
- InvestmentWebView.addEventListener('buttonclick', successCallback, errorCallback)
- {"type":"buttonclick","id": @buttonid@ }
 @buttonid@ 是使用者按下的button id

3. 切換網頁都會回傳網址
- InvestmentWebView.addEventListener('loadstart', successCallback, errorCallback)
- {"type":"loadstart","url": @url@ }
@url@ 是切換後的目的網址

4. 支援從cordova上層直接關閉webview
- InvestmentWebView.close()

5. 開啟網址
- InvestmentWebView.openWeb(title, url, type, noteButtonString)
title(string): 手機上webview標題文字
url(string): 網址
type(int): 特定說明文件
noteButtonString(string): 說明按鈕要顯示給用戶的文字