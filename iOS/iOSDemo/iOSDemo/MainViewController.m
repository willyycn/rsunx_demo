//
//  MainViewController.m
//  iOSDemo
//
//  Created by willyy on 2018/10/22.
//  Copyright © 2018年 willyy. All rights reserved.
//

#import "MainViewController.h"
#import <RSAppSDK/RSunKit.h>
#import "Operation/OpHandler.h"
#import "SVProgressHUD/SVProgressHUD.h"
@interface MainViewController ()
@property (nonatomic, copy) NSString * tk_sum;
@property (nonatomic, copy) NSString * prikey;
@property (nonatomic, copy) NSString * challenge;
@property (weak, nonatomic) IBOutlet UITextField *tokenTF;
@property (weak, nonatomic) IBOutlet UITextField *passwdTF;
@property (weak, nonatomic) IBOutlet UITextField *messageTF;
@property (weak, nonatomic) IBOutlet UIButton *generateBTN;
@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.challenge = [[RSunKit sharedKit] getChallenge];
}

- (IBAction)generate:(id)sender {
    [SVProgressHUD showWithStatus:@"获取..."];
    NSString * hashpass = [RSunKit hash_passwd:self.passwdTF.text salt:@"my_salt"];
    [[OpHandler sharedHandler]generateWithToken:self.tokenTF.text withPassword:hashpass withChallenge:self.challenge withHandler:^(NSDictionary * res) {
        [SVProgressHUD dismiss];
        self.tk_sum = [res[@"tk_sum"] copy];
        self.prikey = [res[@"prikey"] copy];
        self.challenge = [res[@"challenge"] copy];
        self.generateBTN.backgroundColor = [UIColor whiteColor];
        [self.generateBTN setUserInteractionEnabled:NO];
    }];
}

- (IBAction)send_msg:(id)sender {
    if (!self.generateBTN.userInteractionEnabled) {
        NSString * hashpass = [RSunKit hash_passwd:self.passwdTF.text salt:@"my_salt"];
        NSDictionary *rsunx = [[RSunKit sharedKit] rsunxSignature:self.tokenTF.text passwd:hashpass challenge:self.challenge t_chk_sum:self.tk_sum prikey:self.prikey param:ServerParam];
        [[OpHandler sharedHandler] verifySign:rsunx[@"signature"] withToken:self.tokenTF.text andPasswd:hashpass andCheck:self.tk_sum withHandler:^(NSDictionary *res) {
            dispatch_async(dispatch_get_main_queue(), ^{
                UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"验签结果" message:[NSString stringWithFormat:@"%d",[res[@"verify"]intValue]] preferredStyle:UIAlertControllerStyleAlert];
                [alertController addAction:[UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDestructive handler:nil]];
                [self presentViewController:alertController animated:YES completion:nil];
            });
        }];
    }
}
@end
