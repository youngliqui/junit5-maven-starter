package junit.dao;

import by.youngliqui.dao.UserDao;

public class UserDaoMock extends UserDao {
    @Override
    public boolean delete(Integer userId) {
        return false;
    }
}
