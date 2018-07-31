/********* InvestmentWebView.m Cordova Plugin Implementation *******/
#import "InvestmentWebView.h"

@implementation InvestmentWebView

- (void)web_url:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    _targetVC = [[WebViewController alloc] init];
    
    NSString* echo = [command.arguments objectAtIndex:0];
    NSString* title = [echo valueForKey:@"title"];
    NSString* url = [echo valueForKey:@"url"];
    NSString* type = [echo valueForKey:@"type"];
    NSString* noteButtonString = [echo valueForKey:@"noteButtonString"];
//     NSLog(@"title: %@", title );
//     NSLog(@"url: %@", url);
//     NSLog(@"type: %@", type);
    
    if (echo != nil) {
        UIStoryboard* storyboard = [UIStoryboard storyboardWithName:@"WebView" bundle:nil];
        _targetVC = [storyboard instantiateViewControllerWithIdentifier:@"webViewController"];
        _targetVC.theTitle = title;
        _targetVC.theUrl = url;
        _targetVC.theType = type;
        _targetVC.theNoteButtonString = noteButtonString;
        self.callbackId = command.callbackId;
        UINavigationController* nav = [[UINavigationController alloc] initWithRootViewController:_targetVC];
        [self.viewController presentViewController:nav animated:YES completion:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    
    _targetVC.navigationDelegate = self;
}

// 發送更新給前台UI, 提供給 webView 專用
- (void)sendUpdate:(NSDictionary *)object
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                  messageAsDictionary:object];
    [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)close:(CDVInvokedUrlCommand *)command
{
    [_targetVC close];
}

- (void)remindAlertDialog:(CDVInvokedUrlCommand *)command
{
    self.callbackId_remindAlertDialog=command.callbackId;
    
    NSString* echo = [command.arguments objectAtIndex:0];
    NSString* remindAlertDialog_message = [echo valueForKey:@"message"];
    NSString* remindAlertDialog_title = [echo valueForKey:@"title"];
    NSString* remindAlertDialog_buttonName = [echo valueForKey:@"buttonName"];
    [_targetVC remindAlertDialog:remindAlertDialog_message title:remindAlertDialog_title buttonName:remindAlertDialog_buttonName];
}

// 發送更新給前台UI, 提供給 延遲登出 使用
-(void)sendUpdate_remindAlertDialog
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [pluginResult setKeepCallback:[NSNumber numberWithBool:NO]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId_remindAlertDialog];
}

// 設定語言
- (void)language:(CDVInvokedUrlCommand *)command{
    NSLog(@"investmentWebView language");
    [_targetVC  language:[command.arguments objectAtIndex:0]];
}
@end
