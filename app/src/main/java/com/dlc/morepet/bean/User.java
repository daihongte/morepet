package com.dlc.morepet.bean;

/**
 * Auther by winds on 2017/2/24
 * Email heardown@163.com
 */
public class User {
    public String openid;
    public String unionid;
    public String nickname;
    public String language;
    public String sex;
    public String province;
    public String country;
    public String headimgurl;

    @Override
    public String toString() {
        return "User{" +
                "openid='" + openid + '\'' +
                ", unionid='" + unionid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", language='" + language + '\'' +
                ", sex='" + sex + '\'' +
                ", province='" + province + '\'' +
                ", country='" + country + '\'' +
                ", headimgurl='" + headimgurl + '\'' +
                '}';
    }
}
