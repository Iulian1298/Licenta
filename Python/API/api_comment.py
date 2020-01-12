from sqlalchemy import asc, desc

from API import *
from API.api_check import check_token
from database import db
from models.comments import Comment
from models.users import User
from models.services import Service


@app.route("/comments/getById/<commentId>", methods=['GET'])
def getCommentById(commentId):
    comment = Comment.query.filter_by(id=commentId)
    if comment.first():
        commentDict = comment.first().toDict()
        # print(comment.first())
        owner = User.query.filter_by(id=commentDict["userId"])
        result = {}
        result["id"] = commentDict["id"]
        result["comment"] = commentDict["comment"]
        result["creationTime"] = commentDict["creationTime"]
        result["rating"] = commentDict["rating"]
        result["ownerName"] = owner.first().toDict()["fullName"]
        result["serviceId"] = commentDict["serviceId"]
        result["ownerId"] = commentDict["userId"]
        result["profileImage"] = owner.first().toDict()["imagePath"]
        # print(result)
        response = make_response(jsonify({"status": "Found", "comment": result}))
        response.headers.set('Content-Length', len({"status": "Found", "comment": result}))
        return make_response(jsonify({"status": "Found", "comment": result}))
    return make_response(jsonify({"status": "NotFound"}), status.HTTP_404_NOT_FOUND)


@app.route("/comments/addComment", methods=['POST'])
@check_token
def addComment():
    try:
        comment = Comment(id=unicode(uuid.uuid4()),
                          userId=request.json['userId'],
                          serviceId=request.json['serviceId'],
                          comment=request.json['comment'],
                          creationTime=request.json['creationTime'],
                          rating=request.json["rating"])
        serviceAllRatings = [i[0] for i in
                             Comment.query.with_entities(Comment.rating).filter_by(
                                 serviceId=request.json['serviceId']).all()]
        newRating = float(
            format((sum(serviceAllRatings) + float(request.json["rating"])) / (len(serviceAllRatings) + 1), '.2f'))
        db.session.query(Service).filter(Service.id == request.json['serviceId']).update({Service.rating: newRating},
                                                                                         synchronize_session=False)
        # setattr(service, 'rating', newRating)
        print(comment)
        db.session.add(comment)
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not create"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Created", "newRating": newRating}), status.HTTP_201_CREATED)


@app.route("/comments/forService/<serviceId>/getIdsBetween/offset/<offset>/limit/<limit>", methods=['GET'])
def getCommentsIdsBetweenForService(serviceId, offset, limit):
    serviceAllCommentsId = [i[0] for i in
                            Comment.query.with_entities(Comment.id).filter_by(serviceId=serviceId).order_by(
                                desc(Comment.creationTime)).offset(offset).limit(limit).all()]
    # print(serviceAllCommentsId)
    return make_response(jsonify({"Ids": serviceAllCommentsId}), status.HTTP_200_OK)


@app.route("/comments/deleteById/<commentId>/serviceId/<serviceId>", methods=['DELETE'])
@check_token
def deleteCommentById(commentId, serviceId):
    try:
        comment = Comment.query.filter_by(id=commentId)
        serviceAllRatings = [i[0] for i in
                             Comment.query.with_entities(Comment.rating).filter_by(serviceId=serviceId).all()]
        newRating = float(
            format((sum(serviceAllRatings) - comment.first().toDict()["rating"]) / (len(serviceAllRatings) - 1), '.2f'))
        db.session.query(Service).filter(Service.id == serviceId).update({Service.rating: newRating},
                                                                         synchronize_session=False)
        comment.delete()
        db.session.commit()
    except Exception as e:
        print(e)
        return make_response(jsonify({"status": "Could not delete"}), status.HTTP_409_CONFLICT)
    return make_response(jsonify({"status": "Deleted", "newRating": newRating}), status.HTTP_200_OK)


@app.route("/comments/forService/<serviceId>/withRatingStars/<ratingCriteria>/getIdsBetween/"
           "offset/<offset>/limit/<limit>", methods=['GET'])
def getCommentsIdsBetweenWithRatingStarForService(serviceId, ratingCriteria, offset, limit):
    if (int(ratingCriteria) == 0):
        serviceAllCommentsId = [i[0] for i in
                                Comment.query.with_entities(Comment.id).filter_by(serviceId=serviceId).order_by(
                                    desc(Comment.creationTime)).offset(offset).limit(limit).all()]
    else:
        serviceAllCommentsId = [i[0] for i in
                                Comment.query.with_entities(Comment.id).filter_by(serviceId=serviceId,
                                                                                  rating=ratingCriteria).order_by(
                                    desc(Comment.creationTime)).offset(offset).limit(limit).all()]
    return make_response(jsonify({"Ids": serviceAllCommentsId}), status.HTTP_200_OK)
