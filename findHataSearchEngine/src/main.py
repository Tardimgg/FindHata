from concurrent import futures
import profile_pb2_grpc
import grpc

from Vectorizer import VectorizerImpl


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    profile_pb2_grpc.add_VectorizationServiceServicer_to_server(VectorizerImpl(), server)
    server.add_insecure_port("[::]:80")
    server.start()
    server.wait_for_termination()

serve()
