from sqlalchemy import event

from API import *

from flask_sqlalchemy import SQLAlchemy



project_path = os.path.dirname(os.path.abspath(__file__))
database_file = "sqlite:///{}".format(os.path.join(project_path, "YCM.db"))
app.config["SQLALCHEMY_DATABASE_URI"] = database_file
db = SQLAlchemy(app)


event.listens_for(db.get_engine(), 'connect')
def create_math_functions_on_connect(dbapi_connection, connection_record):
    dbapi_connection.create_function('sin', 1, math.sin)
    dbapi_connection.create_function('cos', 1, math.cos)
    dbapi_connection.create_function('acos', 1, math.acos)
    dbapi_connection.create_function('radians', 1, math.radians)