var cordova = require('cordova');
var channel = require('cordova/channel');
var exec = require('cordova/exec')
var mylanguageJson = {};

function InvestmentWebView() {
    this.channels = {
        'loadstart': cordova.addWindowEventHandler('loadstart'),
        'loadstop': cordova.addWindowEventHandler('loadstop'),
        'buttonclick': cordova.addWindowEventHandler('buttonclick'),
        'touch': cordova.addWindowEventHandler('touch')
    };
}

InvestmentWebView.prototype = {
    openWeb : function (title, url, type, noteButtonString, sslPinning) {
    console.log('corodva InvestmentWebView URL: '+url)
    exec(fireEvent, iab._error, 'InvestmentWebView', 'web_url', [{title: title, url: url, type: type, noteButtonString: noteButtonString, sslPinning:sslPinning}])
    },
    close : function(){
    exec(null, null, 'InvestmentWebView', 'close',[])
    },
    addEventListener: function(eventname, successCallback, failCallback){
    // 註冊事件
    try {
        this.channels[eventname].subscribe(successCallback);
    } catch (err) {
        if(failCallback) {
            failCallback(err)
        } else {
            iab._error(err)
        }
    }
    },
    _error: function (e) {
    console.log('Android investmentWebView error callback : ' + e);
    // webView開啟失敗, 此時的alertDialog是在cordova上方
    try {
        navigator.notification.alert(
            e.toString(), // message
            '', // callback
            getLanguageText('confirmTitle'), // title
            getLanguageText('confirmOK') // buttonName
        )
    } catch(err){}
    },
    remindAlertDialog: function(message, callback, title, buttonName){
        // webView開啟成功, aloertDialog必須要在webView之內
        // 錯誤不用回傳
        exec(callback, null, 'InvestmentWebView', 'remindAlertDialog',[{message: message, title: title, buttonName: buttonName}])
    },
    // 設定語言
    language: function (languageJson) {
        mylanguageJson = languageJson;
        exec(null, null, 'InvestmentWebView', 'language', [languageJson]);
    }
}

// 取得plugin狀態變更, 通知前台的js
function fireEvent(info) {
    if (info) {
        if(iab.channels[info.type]!=null) {
            // 啟動事件
            iab.channels[info.type].fire(info);
        }
    }
}
// 取得對應文字
function getLanguageText(key){
     if(mylanguageJson[key])
        return mylanguageJson[key];
     else
        return key;
 }
var iab = new InvestmentWebView()
module.exports = iab