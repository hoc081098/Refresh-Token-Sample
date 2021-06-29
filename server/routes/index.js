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

const refreshTokensSet = new Set();

router.post('/login', function (req, res, next) {
  const { username, password } = req.body;
  if (username === 'hoc081098' && password === '123456') {
    const payload = {
      id: '#1',
      username,
    };

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

    res.status(200).json({
      token,
      refreshToken,
      ...payload,
    });
  } else {
    res.status(401);
  }
});

router.post('/refresh-token', function (req, res, next) {
  const { refreshToken } = req.body;

  try {
    jwt.verify(refreshToken, refreshSecret)
  } catch (e) {

  }
});

module.exports = router;
