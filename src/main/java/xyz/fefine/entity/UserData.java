package xyz.fefine.entity;

import java.util.List;

/**
 * Created by feng_sh on 17-6-1.
 * 测试jsonData注解的用户类
 */
public class UserData {

    private String userName;
    private int age;
    private List<String> some;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getSome() {
        return some;
    }

    public void setSome(List<String> some) {
        this.some = some;
    }
}
