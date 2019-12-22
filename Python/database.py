from API import *

from flask_sqlalchemy import SQLAlchemy

projct_path = os.path.dirname(os.path.abspath(__file__))
database_file = "sqlite:///{}".format(os.path.join(projct_path, "YCM.db"))
app.config["SQLALCHEMY_DATABASE_URI"] = database_file
db = SQLAlchemy(app)
