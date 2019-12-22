from API import *

def check_token(func):
    @wraps(func)
    def decorated(*args, **kwargs):
        token = request.headers['Authorization']
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'])
        except:
            return make_response(jsonify({'message': "Invalid token"}), status.HTTP_403_FORBIDDEN)

        return func(*args, **kwargs)

    return decorated
