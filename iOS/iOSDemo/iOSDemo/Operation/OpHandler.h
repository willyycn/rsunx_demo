//
//  OpHandler.h
//  iOSDemo
//
//  Created by willyy on 2018/5/30.
//  Copyright © 2018年 willyy. All rights reserved.
//

#import "AFHTTPSessionManager.h"
extern NSString * const ServerParam;
@interface OpHandler : AFHTTPSessionManager

+ (instancetype _Nonnull)sharedHandler;
- (void)generateWithToken:(NSString *_Nonnull)token withPassword:(NSString * )passwd withChallenge:(NSString *_Nonnull)challenge withHandler:(void(^)(NSDictionary* res))handler;
- (void)verifySign:(NSString * _Nonnull)signature withToken:(NSString *_Nonnull)token andPasswd:(NSString *_Nonnull)passwd andCheck:(NSString *_Nonnull)check withHandler:(void(^)(NSDictionary* res))handler;
@end
