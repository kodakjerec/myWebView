//
//  WebViewController.m
//  DBS_Omni
//
//  Created by Jason Chen on 2017/12/2.
//  Copyright © 2017年 Jason Chen. All rights reserved.
//
#import "InvestmentWebView.h"

@interface WebViewController ()

@end

@implementation WebViewController {
    NSMutableArray* languageJson;  // 系統預設中英字串
    NSString* locale; // 地區
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    // callback send URL
    NSString *url = request.URL.absoluteString;

    if(url!=nil) {
        [self.navigationDelegate sendUpdate:@{@"type":@"loadstart",@"url": url}];
    }
    
    return YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    // 塞入JavaScript
    [self.webView stringByEvaluatingJavaScriptFromString:[self addMyClickCallBackJs]];
    
    // 調用原生function
    JSContext *context = [[JSContext alloc] init];
    context = [self.webView valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
    context.exceptionHandler = ^(JSContext *context, JSValue *exceptionValue){
        context.exception = exceptionValue;
        NSLog(@"Error: %@",exceptionValue);
    };
    context[@"my"] = self;

}
- (void)myClick:(NSString *)id {
//    NSLog(@"call OC success %@", id);
    [self.navigationDelegate sendUpdate:@{@"type":@"buttonclick",@"id":id}];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.labelTitle setText:self.theTitle];
    // 如果沒有指定title => Hide title
    if(self.theTitle.length <= 0){
        [self.topView setHidden:true];
        self.topViewHeight.constant = 0;
        [UIView animateWithDuration:0.3 animations:^{
            [self.view layoutIfNeeded];
        }];
    }
    
    if(self.theNoteButtonString != nil && ![self.theNoteButtonString isEqualToString: @""]) {
        [self.btnNote setTitle:self.theNoteButtonString forState:UIControlStateNormal];
    } else {
        // 如果沒有指定說明文字 => Hide btnNote
        [self.btnNote setHidden: true];
    }
    
    // // 去除空白和換行符號
    // NSString *urlString = [self.theUrl stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    // // 把特殊符號換成%%符號
    // urlString = [urlString stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    NSURL *url = [NSURL URLWithString:self.theUrl];
    NSURLRequest* request = [NSURLRequest requestWithURL:url];
    
    [self.webView loadRequest:request];
    
    // add webview event
    self.webView.delegate = self;
    
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
    [self.webView addGestureRecognizer:webViewSwipeUp];
    [self.webView addGestureRecognizer:webViewSwipeLeft];
    [self.webView addGestureRecognizer:webViewSwipeRight];
    [self.webView addGestureRecognizer:webViewSwipeDown];
    
    //disable Picture in Picture
    self.webView.allowsPictureInPictureMediaPlayback=false;
    
    // add tap event
    UITapGestureRecognizer *webViewTapped = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapAction:)];
    webViewTapped.numberOfTapsRequired = 1;
    webViewTapped.delegate = self;
    [self.webView addGestureRecognizer:webViewTapped];
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
//    NSLog(@"swiped %@", direction);
    double currentTime = CACurrentMediaTime();
    [self.navigationDelegate sendUpdate:@{@"type":@"touch",@"lastTouchTime":@(currentTime).stringValue}];
    
}
- (void)tapAction:(UITapGestureRecognizer *)sender
{
//    CGPoint point = [sender locationInView:self.view];
//    NSLog(@"touched x:%f y:%f", point.x, point.y);
    double currentTime = CACurrentMediaTime();
    [self.navigationDelegate sendUpdate:@{@"type":@"touch",@"lastTouchTime":@(currentTime).stringValue}];
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
//    NSLog(@"%d", self.webView.pageLength);
    if([self.webView canGoBack]) {
        [self.webView goBack];
        [self.btnNote setHidden:false];
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
            switch ([self.theType integerValue]) {
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
            switch ([self.theType integerValue]) {
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
    
    [self.webView loadRequest:urlRequest];
    [self.btnNote setHidden:true];
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
    js = @"function myClick(t){if(null!=t.target.id){var e=t.target;\"button\"==e.tagName.toLowerCase()?my.myClick(e.id):\"button\"==(e=e.parentElement).tagName.toLowerCase()&&my.myClick(e.id)}}document.addEventListener(\"click\",myClick,!0);";
    
    /*
     function myClick(event){
     if(event.target.id != null){
     var obj = event.target;
     if(obj.tagName.toLowerCase()==\"button\") {
     my.myClick(obj.id);
     }
     else {
     obj = obj.parentElement;
     if(obj.tagName.toLowerCase()==\"button\") {
     my.myClick(obj.id);
     }
     }
     }
     }
     document.addEventListener(\"click\",myClick,true);
     */
    return js;
}

// 9分鐘提醒視窗, 返回效果與showAlertDialog不同
- (void)remindAlertDialog:(NSString *)message title:(NSString *)title buttonName:(NSString *)buttonName
{
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:title
                                                                   message:message
                                                            preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction* defaultAction = [UIAlertAction actionWithTitle:buttonName style:UIAlertActionStyleDefault
                                                          handler: ^(UIAlertAction *action) {
                                                              [self.navigationDelegate sendUpdate_remindAlertDialog];
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

// #endregion
@end
