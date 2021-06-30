var express = require('express');
var router = express.Router();
const jwt = require('jsonwebtoken');

/* GET home page. */
router.get('/', function (req, res, next) {
  res.render('index', { title: 'Express' });
});

const secret = 'my-secret';
const expiresIn = '1m';

const refreshSecret = 'my-refresh-secret';
const refreshExpiresIn = '1d';

const refreshTokensMap = {};

router.post('/login', function (req, res, next) {
  const { username, password } = req.body;
  if (username === 'hoc081098' && password === '123456') {
    const payload = {
      id: '#1',
      username,
    };

    try {
      const token = jwt.sign(
        payload,
        secret,
        { expiresIn },
      );

      const refreshToken = jwt.sign(
        payload,
        refreshSecret,
        { expiresIn: refreshExpiresIn },
      );

      refreshTokensMap[username] = refreshToken;

      res.status(200).json({
        token,
        refreshToken,
        ...payload,
      });
    } catch (e) {
      return res.status(500).json({ message: 'Server error' });
    }
  } else {
    return res.status(401).json({ message: 'Invalid credential' });
  }
});

router.post('/refresh-token', function (req, res, next) {
  const { refreshToken, username } = req.body;

  if (!refreshToken) {
    return res.status(401).json({ message: 'Require refreshToken' });
  }
  if (!username) {
    return res.status(401).json({ message: 'Require username' });
  }

  if (refreshToken !== refreshTokensMap[username]) {
    return res.status(401).json({ message: "Refresh token is not in database!" });
  }

  let payload;
  try {
    payload = jwt.verify(refreshToken, refreshSecret)
  } catch (e) {
    delete refreshTokensMap[username];
    return res.status(401).json({
      message: "Refresh token was expired. Please make a new signin request",
    });
  }

  try {
    const token = jwt.sign(
      payload,
      secret,
      { expiresIn },
    );
    res.status(200).json({
      token,
      refreshToken,
      ...payload,
    });
  } catch (e) {
    return res.status(500).json({ message: 'Server error' });
  }
});

module.exports = router;
