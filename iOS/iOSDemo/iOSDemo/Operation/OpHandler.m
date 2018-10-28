//
//  OpHandler.m
//  iOSDemo
//
//  Created by willyy on 2018/5/30.
//  Copyright © 2018年 willyy. All rights reserved.
//

#import "OpHandler.h"
#import <RSAppSDK/RSunKit.h>
NSString * const ServerUrl = @"http://10.10.252.13:8000";
NSString * const ServerParam = @"412a9e72f2120b69d685200d834d4a3e5349c09ba00583f762929af5c6379c6dcdab5ad96b5ee53e5ad0ac912f57a347da3ff4fb474847e534040086c0830b2d";
NSString * const ServerChkSum = @"5adad94944b686f8b47d237681d4f0ce5fc89d4d14b1f4a8d38866ef37031e1c654d9826bc67b931d0836d8676957d959e40560054c3a6e4601754698f7c99ca";
NSString * const ServerToken = @"{\"neo_server\":\"willyy@R-Sun-X\"}";

@implementation OpHandler
+ (instancetype _Nonnull)sharedHandler{
    static id _sharedInstance = nil;
    static dispatch_once_t oncePredicate;
    dispatch_once(&oncePredicate, ^{
        _sharedInstance = [[[self class] alloc] init];
    });
    return _sharedInstance;
}

-(id)init
{
    self=[super init];
    if (self) {
        //init
        self = [self initWithBaseURL:[NSURL URLWithString:ServerUrl]];
        self.requestSerializer = [AFHTTPRequestSerializer serializer];
        self.responseSerializer = [AFJSONResponseSerializer serializer];
        self.requestSerializer.timeoutInterval = 20.f;
        self.requestSerializer.cachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
        self.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"application/x-www-form-urlencoded; charset=UTF-8", nil];
    }
    return self;
}

- (void)generateWithToken:(NSString *_Nonnull)token withPassword:(NSString * )passwd withChallenge:(NSString *_Nonnull)challenge withHandler:(void(^)(NSDictionary* res))handler{
    if(passwd.length==0){
        passwd = @"";
    }
    NSDictionary * rsunx = [[RSunKit sharedKit] rsunxInitiate:token passwd:passwd];
    NSString * secret = rsunx[@"secret"];
    NSString * element = rsunx[@"element"];
    rsunx = [[RSunKit sharedKit] rsunxEncrypt:token token:ServerToken passwd:nil t_chk_sum:ServerChkSum param:ServerParam];
    [[OpHandler sharedHandler] POST:@"/demo/generate" parameters:@{@"token":rsunx[@"cipher"],@"passwd":passwd,@"element":element,@"challenge":challenge} progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        if ([responseObject[@"status"] integerValue] == 1) {
            BOOL v = [[RSunKit sharedKit] rsunxVerify:ServerToken passwd:nil challenge:challenge signature:responseObject[@"signature"] t_chk_sum:ServerChkSum param:ServerParam];
            if (v == 1) {
                NSDictionary * x = [[RSunKit sharedKit] rsunxGenerate:token passwd:passwd seed:responseObject[@"seed"] secret:secret];
            handler(@{@"prikey":x[@"prikey"],@"tk_sum":x[@"t_chk_sum"],@"challenge":responseObject[@"rand"]});
            }
        }
    } failure:nil];
}
- (void)verifySign:(NSString * _Nonnull)signature withToken:(NSString *_Nonnull)token andPasswd:(NSString *_Nonnull)passwd andCheck:(NSString *_Nonnull)check withHandler:(void(^)(NSDictionary* res))handler{
    [[OpHandler sharedHandler] POST:@"/demo/verify" parameters:@{@"sign":signature,@"passwd":passwd,@"check":check,@"token":token} progress:nil success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        handler(responseObject);
    } failure:nil];
}
@end
