//
//  InvestmentWebView.h
//  SCB_kota
//
//  Created by 簡克達 on 2018/7/3.
//

#import <Cordova/CDV.h>
#import <JavaScriptCore/JavaScriptCore.h>
@class WebViewController;

@interface InvestmentWebView : CDVPlugin

@property (nonatomic, copy) NSString* callbackId;
@property (nonatomic, copy) NSString* callbackId_remindAlertDialog;
@property (nonatomic, retain) WebViewController* targetVC;
- (void)web_url:(CDVInvokedUrlCommand*)command;
- (void)close:(CDVInvokedUrlCommand*)command;
- (void)remindAlertDialog:(CDVInvokedUrlCommand*)command;
- (void)sendUpdate_remindAlertDialog;
- (void)sendUpdate:(NSDictionary *)object;
- (void)language:(CDVInvokedUrlCommand*)command;

@end

@protocol JavaScriptDelegate <JSExport>

- (void) myClick:(NSString *)id;

@end

@interface WebViewController : UIViewController <UIWebViewDelegate, UIGestureRecognizerDelegate, JavaScriptDelegate>

@property (nonatomic, weak) InvestmentWebView* navigationDelegate;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *topViewHeight;
@property (strong, nonatomic) IBOutlet UIWebView* webView;
@property (strong, nonatomic) IBOutlet UIView* topView;
@property (strong, nonatomic) IBOutlet UIButton* btnClose;
@property (weak, nonatomic) IBOutlet UILabel *labelTitle;
@property (weak, nonatomic) IBOutlet UIButton *btnNote;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *equalHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *alignY;
@property (strong, nonatomic) NSString* theTitle;
@property (strong, nonatomic) NSString* theUrl;
@property (strong, nonatomic) NSString* theType;
@property (strong, nonatomic) NSString* theNoteButtonString;

- (IBAction)btnCloseClick:(id)sender;
- (void)remindAlertDialog:(NSString*)message title:(NSString*)title buttonName:(NSString*)buttonName;
- (void)close;
- (void)language:(NSMutableArray*) obj;

@end
