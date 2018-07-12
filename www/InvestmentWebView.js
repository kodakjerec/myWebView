var cordova = require('cordova');
    var channel = require('cordova/channel');
    var exec = require('cordova/exec')

    function InvestmentWebView() {
        this.channels = {
            'loadstart': cordova.addWindowEventHandler('loadstart'),
            'loadstop': cordova.addWindowEventHandler('loadstop'),
            'buttonclick': cordova.addWindowEventHandler('buttonclick'),
            'touch': cordova.addWindowEventHandler('touch')
        };
    }

    InvestmentWebView.prototype = {
      openWeb : function (title, url, type, noteButtonString) {
        console.log('corodva InvestmentWebView URL: '+url)
        exec(fireEvent, iab._error, 'InvestmentWebView', 'web_url', [{title: title, url: url, type: type, noteButtonString: noteButtonString}])
      },
      close : function(){
        exec(null, iab._error, 'InvestmentWebView', 'close',[])
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
        try {
            navigator.notification.alert(
                e.toString(), // message
                '', // callback
                'Android WebView fail', // title
                'CLOSE' // buttonName
            )
        } catch(err){}
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

    var iab = new InvestmentWebView()
    module.exports = iab