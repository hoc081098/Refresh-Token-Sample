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

function checkToken(req) {
  const authorization = req.headers.authorization;
  if (!authorization) {
    throw new Error('Require authorization header');
  }
  const [type, token] = authorization.split(' ');
  if (type === 'Bearer') {
    if (!token) {
      throw new Error('Require token in header');
    }

    try {
      return jwt.verify(token, secret);
    } catch (e) {
      console.log({ e });
      throw new Error('Invalid token');
    }
  } else {
    throw new Error('Invalid authorization type');
  }
}

router.get('/check-auth', function (req, res, next) {
  try {
    res.status(200).json(checkToken(req));
  } catch (e) {
    res.status(401).json({ message: e.message });
  }
});

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
      console.log({ e });
      return res.status(500).json({ message: 'Server error' });
    }
  } else {
    return res.status(401).json({ message: 'Invalid credential' });
  }
});

router.post('/refresh-token', async function (req, res, next) {
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
    payload = jwt.verify(refreshToken, refreshSecret);
  } catch (e) {
    console.log({ e });
    delete refreshTokensMap[username];
    return res.status(401).json({
      message: "Refresh token was expired. Please make a new signin request",
    });
  }
  payload = {
    username: payload.username,
    id: payload.id,
  };

  await new Promise((resolve) => setTimeout(resolve, 2000));
  console.log(`refresh-token ${JSON.stringify(payload)}`);

  try {
    const token = jwt.sign(
      payload,
      secret,
      { expiresIn },
    );
    res.status(200).json({
      token,
      refreshToken,
    });
  } catch (e) {
    console.log({ e });
    return res.status(500).json({ message: 'Server error' });
  }
});

router.get('/demo', async function (req, res, next) {
  try {
    checkToken(req);
  } catch (e) {
    return res.status(401).json({ message: e.message });
  }
  const count = req.query.count;
  await new Promise((resolve) => setTimeout(resolve, 2000));
  res.status(200).json('' + count + ' ' + (new Date().toISOString()));
});

module.exports = router;
