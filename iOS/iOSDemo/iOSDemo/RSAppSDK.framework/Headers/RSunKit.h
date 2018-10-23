//
//  RSunKit.h
//  RSAppDev
//
//  Created by willyy on 18/02/2018.
//  Copyright © 2018 willyy. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RSunKit : NSObject
+ (instancetype)sharedKit;

/**
 秘钥初始化
 @param token 用户自定义token
 @param passwd token对应的使用密码
 @return status状态, element交互要素(发送至对方), secret保留秘钥
 */
-(NSDictionary *)rsunxInitiate:(NSString *)token passwd:(NSString *)passwd;

/**
 私钥及隐证书产生
 @param token 自定义token
 @param passwd token对应的使用密码
 @param seed 种子(对方返回)
 @param secret 保留秘钥
 @return status状态, prikey私钥, t_chk_sum token校验
 */
-(NSDictionary *)rsunxGenerate:(NSString *)token passwd:(NSString *)passwd seed:(NSString *)seed secret:(NSString *)secret;

/**
 使用私钥等信息对挑战签名
 @param token 私钥对应的token
 @param passwd token对应的使用密码
 @param challenge 挑战信息
 @param t_chk_sum token校验
 @param prikey 私钥
 @param param 系统公共参数
 @return status状态, signature签名
 */
-(NSDictionary *)rsunxSignature:(NSString *)token passwd:(NSString *)passwd challenge:(NSString *)challenge t_chk_sum:(NSString *)t_chk_sum prikey:(NSString *)prikey param:(NSString *)param;

/**
 使用信息来验证签名
 @param token 签名方对应的token
 @param passwd token对应的使用密码
 @param challenge 挑战信息
 @param signature 签名信息
 @param t_chk_sum 签名方token校验
 @param param 系统公共参数
 @return verify验签结果(1为验签通过)
 */
-(BOOL)rsunxVerify:(NSString *)token passwd:(NSString *)passwd challenge:(NSString *)challenge signature:(NSString *)signature t_chk_sum:(NSString *)t_chk_sum param:(NSString *)param;

/**
 使用公钥等信息加密
 @param plain 明文信息
 @param token 公钥方对应的token
 @param passwd token对应的使用密码
 @param t_chk_sum 公钥方对应的token校验
 @param param 系统公共参数
 @return status状态, cipher密文
 */
-(NSDictionary *)rsunxEncrypt:(NSString *)plain token:(NSString *)token passwd:(NSString *)passwd t_chk_sum:(NSString *)t_chk_sum param:(NSString *)param;

/**
 使用私钥解密
 @param cipher 密文
 @param prikey 私钥
 @return status状态, plain明文
 */
-(NSDictionary *)rsunxDecrypt:(NSString *)cipher prikey:(NSString *)prikey;

/**
 获取挑战
 @return 挑战信息
 */
- (NSString *)getChallenge;

/**
 对passwd的hash化
 @param plain_passwd 原密码
 @param salt 盐
 @return 加盐后hash密码
 */
+ (NSString *)hash_passwd:(NSString *)plain_passwd salt:(NSString *)salt;
@end
