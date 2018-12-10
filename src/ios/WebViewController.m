//
//  WebViewController.m
//  DBS_Omni
//
//  Created by Jason Chen on 2017/12/2.
//  Copyright © 2017年 Jason Chen. All rights reserved.
//
#import "InvestmentWebView.h"

@implementation WebViewController {
    NSMutableArray* languageJson;  // 系統預設中英字串
    NSString* locale; // 地區
}

#pragma mark - WKNavigationDelegate
- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler {
    NSLog(@"decidePolicyForNavigationAction  %@",navigationAction.request.URL.absoluteString);
    // callback send URL
    NSString *url = navigationAction.request.URL.absoluteString;
    
    if(url!=nil) {
        [_investmentWebViewDelegate sendUpdate:@{@"type":@"loadstart",@"url": url}];
    }
    
    //允许跳转
    decisionHandler(WKNavigationActionPolicyAllow);
    //不允许跳转
    //decisionHandler(WKNavigationActionPolicyCancel);
}

- (void)webView:(WKWebView *)webView decidePolicyForNavigationResponse:(WKNavigationResponse *)navigationResponse decisionHandler:(void (^)(WKNavigationResponsePolicy))decisionHandler {
    NSLog(@"decidePolicyForNavigationResponse  %@",navigationResponse.response.URL.absoluteString);
    
    //允许跳转
    decisionHandler(WKNavigationResponsePolicyAllow);
    //不允许跳转
    //decisionHandler(WKNavigationResponsePolicyCancel);
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation{
    // 開啟進度條
    [_progressView setHidden:NO];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation
{
    // 塞入JavaScript
    [webView evaluateJavaScript:[self addMyClickCallBackJs]
                completionHandler:^(id _Nullable ret, NSError * _Nullable error) {
                    if (error!=nil)
                        NSLog(@"addMyClickCallBackJs Error: %@",error);
                }];
    
    // 隱藏進度條
    [_progressView setHidden:YES];
}

- (void)dealloc
{
    // 取消監聽 progress bar
    [_webView removeObserver:self forKeyPath:@"estimatedProgress"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [_labelTitle setText:_theTitle];
    // 如果沒有指定title => Hide title
    if(_theTitle.length <= 0){
        [_topView setHidden:true];
        _topViewHeight.constant = 0;
        [UIView animateWithDuration:0.3 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
    
    if(_theNoteButtonString != nil && ![_theNoteButtonString isEqualToString: @""]) {
        [_btnNote setTitle:_theNoteButtonString forState:UIControlStateNormal];
    } else {
        // 如果沒有指定說明文字 => Hide btnNote
        [_btnNote setHidden: true];
    }
    
    // #region JavaScript

    
    // 加載javaScript, 註冊 handler
    WKWebViewConfiguration *config = [[WKWebViewConfiguration alloc] init];
    config.userContentController = [[WKUserContentController alloc] init];
    
    //声明 hello message handler 协议
    [config.userContentController addScriptMessageHandler:self name:@"myClick"];
    WKWebView *newWebView = [[WKWebView alloc] initWithFrame:_webView.frame configuration:config];
    
    // 将WKWebView添加到视图
    // 插入原本 _webView 的位置
    [self.view insertSubview:newWebView atIndex:1];
    // 從UIViewController 的連結中移除舊的 webView
    [_webView removeFromSuperview];
    // 為了後續程式流程, _webView 設定為 newWebView
    _webView = newWebView;
    // #endregion
    
    // #region 设置访问的URL
    NSURL *url = [NSURL URLWithString:_theUrl];
    // 根据URL创建请求
    NSURLRequest* request = [NSURLRequest requestWithURL:url];
    // WKWebView加载请求
    [_webView loadRequest:request];
    // #endregion 设置访问的URL
    
    // add webview event
    _webView.navigationDelegate = self;
    _webView.UIDelegate = self;
    
    // add swipe event
    UISwipeGestureRecognizer *webViewSwipeUp = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipeAction:)];
    UISwipeGestureRecognizer *webViewSwipeLeft = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipeAction:)];
    UISwipeGestureRecognizer *webViewSwipeRight = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipeAction:)];
    UISwipeGestureRecognizer *webViewSwipeDown = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(swipeAction:)];
    webViewSwipeUp.direction = UISwipeGestureRecognizerDirectionUp;
    webViewSwipeLeft.direction = UISwipeGestureRecognizerDirectionLeft;
    webViewSwipeRight.direction = UISwipeGestureRecognizerDirectionRight;
    webViewSwipeDown.direction = UISwipeGestureRecognizerDirectionDown;
    webViewSwipeUp.delegate = self;
    webViewSwipeLeft.delegate = self;
    webViewSwipeRight.delegate = self;
    webViewSwipeDown.delegate = self;
    [_webView addGestureRecognizer:webViewSwipeUp];
    [_webView addGestureRecognizer:webViewSwipeLeft];
    [_webView addGestureRecognizer:webViewSwipeRight];
    [_webView addGestureRecognizer:webViewSwipeDown];
    
    // add tap event
    UITapGestureRecognizer *webViewTapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapAction:)];
    webViewTapped.numberOfTapsRequired = 1;
    webViewTapped.delegate = self;
    [_webView addGestureRecognizer:webViewTapped];
    
    // add progress bar
    [_webView addObserver:self forKeyPath:@"estimatedProgress" options:NSKeyValueObservingOptionNew context:nil];
}

// 加入觸控偵測
- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}
- (void)swipeAction:(UISwipeGestureRecognizer *)sender
{
    NSString *direction = @"";
    switch (sender.direction) {
        case 1:
            direction = @"Right";
            break;
        case 2:
            direction = @"Left";
            break;
        case 4:
            direction = @"Up";
            break;
        case 8:
            direction = @"Down";
            break;
        default:
            direction = @"error";
            break;
    }
    double currentTime = CACurrentMediaTime();
    [_investmentWebViewDelegate sendUpdate:@{@"type":@"touch",@"lastTouchTime":@(currentTime).stringValue}];
    
}
- (void)tapAction:(UITapGestureRecognizer *)sender
{
    double currentTime = CACurrentMediaTime();
    [_investmentWebViewDelegate sendUpdate:@{@"type":@"touch",@"lastTouchTime":@(currentTime).stringValue}];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    [self.navigationItem hidesBackButton];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)btnCloseClick:(id)sender {
//    NSLog(@"%d", _webView.pageLength);
    if([_webView canGoBack]) {
        [_webView goBack];
        [_btnNote setHidden:false];
    } else {
        [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
}

- (IBAction)btnNoteClick:(id)sender {
    
    NSString* noteFileName = @"";
    int intLocale=0;
    if([locale isEqualToString:@"en"]){
        intLocale=1;
    }
    switch (intLocale) {
        case 1:
            switch ([_theType integerValue]) {
                case 2:
                    noteFileName = @"investment_note_2_en";
                    break;
                case 3:
                    noteFileName = @"investment_note_3_en";
                    break;
                case 4:
                    noteFileName = @"investment_note_4_en";
                    break;
                case 5:
                    noteFileName = @"investment_note_5_en";
                    break;
                case 6:
                    noteFileName = @"investment_note_6_en";
                    break;
                case 7:
                    noteFileName = @"investment_note_7_en";
                    break;
                case 20:
                    noteFileName = @"investment_note_20_en";
                    break;
                default:  //1
                    noteFileName = @"investment_note_1_en";
                    break;
            }
            break;
        default:
            switch ([_theType integerValue]) {
                case 2:
                    noteFileName = @"investment_note_2";
                    break;
                case 3:
                    noteFileName = @"investment_note_3";
                    break;
                case 4:
                    noteFileName = @"investment_note_4";
                    break;
                case 5:
                    noteFileName = @"investment_note_5";
                    break;
                case 6:
                    noteFileName = @"investment_note_6";
                    break;
                case 7:
                    noteFileName = @"investment_note_7";
                    break;
                case 20:
                    noteFileName = @"investment_note_20";
                    break;
                default:  //1
                    noteFileName = @"investment_note_1";
                    break;
            }
            break;
    }
    
    NSURL *url = [[NSBundle mainBundle] URLForResource:noteFileName withExtension:@"html"];
    NSURLRequest *urlRequest = [NSURLRequest requestWithURL:url];
    
    [_webView loadRequest:urlRequest];
    [_btnNote setHidden:true];
}

- (void)close
{
    // Run later to avoid the "took a long time" log message.
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self respondsToSelector:@selector(presentingViewController)]) {
            [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
        } else {
            [self.parentViewController dismissViewControllerAnimated:YES completion:nil];
        }
    });
}

// JS--
- (NSString *)addMyClickCallBackJs {
    NSString *js = @"";
    js = @"function myClick(t){if(null!=t.target.id){var e=t.target;\"button\"==e.tagName.toLowerCase()?window.webkit.messageHandlers.myClick.postMessage(e.id):\"button\"==(e=e.parentElement).tagName.toLowerCase()&&window.webkit.messageHandlers.myClick.postMessage(e.id)}}document.addEventListener(\"click\",myClick,!0);";
    
    /*
     function myClick(event){
     if(event.target.id != null){
     var obj = event.target;
     if(obj.tagName.toLowerCase()==\"button\") {
     window.webkit.messageHandlers.myClick.postMessage(obj.id);
     }
     else {
     obj = obj.parentElement;
     if(obj.tagName.toLowerCase()==\"button\") {
     window.webkit.messageHandlers.myClick.postMessage(obj.id);
     }
     }
     }
     }
     document.addEventListener(\"click\",myClick,true);
     */
    return js;
}
- (void)userContentController:(WKUserContentController *)userContentController didReceiveScriptMessage:(WKScriptMessage *)message{
    //    [self.scriptDelegate userContentController:userContentController didReceiveScriptMessage:message];
    //    if ([message.name isEqualToString:@"myClick"]) {
    NSLog(@"messge.name %@",message.name);
    NSLog(@"message.body %@", message.body);
    
    [_investmentWebViewDelegate sendUpdate:@{@"type":@"buttonclick",@"id":message.body}];
}

// 9分鐘提醒視窗, 返回效果與showAlertDialog不同
- (void)remindAlertDialog:(NSString *)message title:(NSString *)title buttonName:(NSString *)buttonName
{
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:title
                                                                   message:message
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:buttonName style:UIAlertActionStyleDefault
                                                          handler: ^(UIAlertAction *action) {
                                                              [_investmentWebViewDelegate sendUpdate_remindAlertDialog];
                                                          }];
    
    [alert addAction:defaultAction];
    [self presentViewController:alert animated:YES completion:nil];
}

// 設定語言
- (void)language:(NSMutableArray*) obj{
    NSLog(@"WebViewCOntroller language");
    languageJson = obj;
    locale = [obj valueForKey:@"locale"];
}
// 取得對應的文字
- (NSString*)getLanguageText:(NSString*)key {
    if([languageJson valueForKey:key]){
        return [languageJson valueForKey:key];
    } else {
        return key;
    }
}

// #region add progress bar
- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context{
    if ([keyPath  isEqual: @"estimatedProgress"]){
        [_progressView setProgress:(float)(_webView.estimatedProgress) animated:YES];
    }
}
// #endregion add progress bar

// #region 螢幕旋轉
- (BOOL)shouldAutorotate{
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations{
    return UIInterfaceOrientationMaskPortrait|UIInterfaceOrientationMaskPortraitUpsideDown;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation{
    return UIInterfaceOrientationPortrait;
}

// #endregion 螢幕旋轉

// #region 私人憑證或http 無法瀏覽問題
// 上架前一定要mark掉
//- (void)webView:(WKWebView *)webView didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition, NSURLCredential * _Nullable))completionHandler{
//    NSURLCredential *credential = [[NSURLCredential alloc] initWithTrust:[challenge protectionSpace].serverTrust];
//    completionHandler(NSURLSessionAuthChallengeUseCredential, credential);
//}
// #endregion 私人憑證或http 無法瀏覽問題
@end
