build: build-all-jar
build-in-docker: build-all-jar-in-docker

clean: clean-docker clean-all-files

clean-docker:
	sudo docker compose down --volumes
	sudo docker rmi -f findhata-gateway-server findhata-auth-server findhata-notification-server findhata-discovery-server findhata-data-base findhata-messaging-server findhata-proposal-server findhata-frontend-server findhata-ingress findhata-tunnel findhata-vectorization-server

build-all-jar: .findHataAuth.fake .findHataGateway.fake .findHataNotificationServer.fake .findHataMessagingServer.fake .findHataProposalServer.fake .findHataServerDiscovery.fake
	sudo docker compose build

build-all-jar-in-docker: findHataAuth-preparing-dep findHataGateway-preparing-dep findHataNotificationServer-preparing-dep findHataMessagingServer-preparing-dep findHataProposalServer-preparing-dep findHataServerDiscovery-preparing-dep
	cp ./build_dep ./findHataAuth
	cp ./build_dep ./findHataGateway
	cp ./build_dep ./findHataMessagingServer
	cp ./build_dep ./findHataNotificationServer
	cp ./build_dep ./findHataProposalServer
	cp ./build_dep ./findHataServerDiscovery

	sudo docker compose -f docker-compose-build-in-docker.yml build


clean-all-files:
	find ./ -maxdepth 1 -name "*.fake" | xargs rm -f
	rm -f ./*/build_dep

	cd databaseCreator; ./gradlew clean; cd ../

	cd result; ./gradlew clean; cd ../

	cd tokenService; ./gradlew clean; cd ../

	cd webApiClient; ./gradlew clean; cd ../
	rm -rf ./webApiClient/libs/*

	cd externalRequestFilter; ./gradlew clean; cd ../
	rm -rf ./externalRequestFilter/libs/*

	cd findHataAuth; ./gradlew clean; cd ../
	rm -rf ./findHataAuth/libs/*
	rm -rf ./findHataAuth/docker/*.jar

	cd findHataGateway; ./gradlew clean; cd ../
	rm -rf ./findHataGateway/libs/*
	rm -rf ./findHataGateway/docker/*.jar

	cd findHataNotificationServer; ./gradlew clean; cd ../
	rm -rf ./findHataNotificationServer/libs/*
	rm -rf ./findHataNotificationServer/docker/*.jar

	cd findHataMessagingServer; ./gradlew clean; cd ../
	rm -rf ./findHataMessagingServer/libs/*
	rm -rf ./findHataMessagingServer/docker/*.jar

	cd findHataProposalServer; ./gradlew clean; cd ../
	rm -rf ./findHataProposalServer/libs/*
	rm -rf ./findHataProposalServer/docker/*.jar

	cd findHataServerDiscovery; ./gradlew clean; cd ../
	rm -rf ./findHataServerDiscovery/docker/*.jar

	cd findHataUploadServer; ./gradlew clean; cd ../
	rm -rf ./findHataUploadServer/docker/*.jar


databaseCreator-preparing-dep:
result-preparing-dep:
tokenService-preparing-dep:

webApiClient-preparing-dep: tokenService-preparing-dep
	mkdir -p ./webApiClient/libs
	cp -r ./tokenService ./webApiClient/libs/tokenService

externalRequestFilter-preparing-dep: tokenService-preparing-dep
	mkdir -p ./externalRequestFilter/libs
	cp -r ./tokenService ./externalRequestFilter/libs/tokenService

findHataAuth-preparing-dep: webApiClient-preparing-dep result-preparing-dep databaseCreator-preparing-dep externalRequestFilter-preparing-dep
	mkdir -p ./findHataAuth/libs
	cp -r ./webApiClient ./findHataAuth/libs/webApiClient
	cp -r ./result ./findHataAuth/libs/result/
	cp -r ./databaseCreator ./findHataAuth/libs/databaseCreator
	cp -r ./externalRequestFilter ./findHataAuth/libs/externalRequestFilter

findHataGateway-preparing-dep: webApiClient-preparing-dep result-preparing-dep
	mkdir -p ./findHataGateway/libs
	cp -r ./webApiClient ./findHataGateway/libs/webApiClient
	cp -r ./result ./findHataGateway/libs/result

findHataNotificationServer-preparing-dep: tokenService-preparing-dep result-preparing-dep databaseCreator-preparing-dep externalRequestFilter-preparing-dep
	mkdir -p ./findHataNotificationServer/libs
	cp -r ./tokenService ./findHataNotificationServer/libs/tokenService
	cp -r ./databaseCreator ./findHataNotificationServer/libs/databaseCreator
	cp -r ./result ./findHataNotificationServer/libs/result
	cp -r ./externalRequestFilter ./findHataNotificationServer/libs/externalRequestFilter

findHataMessagingServer-preparing-dep: webApiClient-preparing-dep result-preparing-dep databaseCreator-preparing-dep externalRequestFilter-preparing-dep
	mkdir -p ./findHataMessagingServer/libs
	cp -r ./webApiClient ./findHataMessagingServer/libs/webApiClient
	cp -r ./databaseCreator ./findHataMessagingServer/libs/databaseCreator
	cp -r ./result ./findHataMessagingServer/libs/result
	cp -r ./externalRequestFilter ./findHataMessagingServer/libs/externalRequestFilter

findHataProposalServer-preparing-dep: result-preparing-dep databaseCreator-preparing-dep externalRequestFilter-preparing-dep
	mkdir -p ./findHataProposalServer/libs
	cp -r ./result ./findHataProposalServer/libs/result
	cp -r ./databaseCreator ./findHataProposalServer/libs/databaseCreator
	cp -r ./externalRequestFilter ./findHataProposalServer/libs/externalRequestFilter
	cp -r ./findHataUploadServer ./findHataProposalServer/libs/findHataUploadServer

findHataServerDiscovery-preparing-dep:

findHataUploadServer-preparing-dep:


.databaseCreator.fake:
	cd ./databaseCreator && ./gradlew shadowJar

	touch .databaseCreator.fake

.result.fake:
	cd ./result && ./gradlew shadowJar

	touch .result.fake

.tokenService.fake:
	cd ./tokenService && ./gradlew shadowJar

	touch .tokenService.fake

.webApiClient.fake: .tokenService.fake
	mkdir -p ./webApiClient/libs
	cp ./tokenService/build/libs/tokenService-1.0-all.jar ./webApiClient/libs/
	cd ./webApiClient && ./gradlew shadowJar

	touch .webApiClient.fake

.externalRequestFilter.fake: .tokenService.fake
	mkdir -p ./externalRequestFilter/libs
	cp ./tokenService/build/libs/tokenService-1.0-all.jar ./externalRequestFilter/libs/

	cd ./externalRequestFilter && ./gradlew shadowJar

	touch .externalRequestFilter.fake

.findHataAuth.fake: .webApiClient.fake .result.fake .databaseCreator.fake .externalRequestFilter.fake
	mkdir -p ./findHataAuth/libs
	cp ./webApiClient/build/libs/webApiClient-1.0-all.jar ./findHataAuth/libs/
	cp ./result/build/libs/result-1.0-all.jar ./findHataAuth/libs/
	cp ./databaseCreator/build/libs/databaseCreator-1.0-all.jar ./findHataAuth/libs/
	cp ./externalRequestFilter/build/libs/externalRequestFilter-1.0-all.jar ./findHataAuth/libs/
	cd ./findHataAuth && ./gradlew copyJar

	touch .findHataAuth.fake


.findHataGateway.fake: .webApiClient.fake .result.fake
	mkdir -p ./findHataGateway/libs
	cp ./webApiClient/build/libs/webApiClient-1.0-all.jar ./findHataGateway/libs/
	cp ./result/build/libs/result-1.0-all.jar ./findHataGateway/libs/
	cd ./findHataGateway && ./gradlew copyJar

	touch .findHataGateway.fake

.findHataNotificationServer.fake: .tokenService.fake .result.fake .databaseCreator.fake .externalRequestFilter.fake
	mkdir -p ./findHataNotificationServer/libs
	cp ./tokenService/build/libs/tokenService-1.0-all.jar ./findHataNotificationServer/libs/
	cp ./databaseCreator/build/libs/databaseCreator-1.0-all.jar ./findHataNotificationServer/libs/
	cp ./result/build/libs/result-1.0-all.jar ./findHataNotificationServer/libs/
	cp ./externalRequestFilter/build/libs/externalRequestFilter-1.0-all.jar ./findHataNotificationServer/libs/
	cd ./findHataNotificationServer && ./gradlew copyJar

	touch .findHataNotificationServer.fake


.findHataMessagingServer.fake: .webApiClient.fake .result.fake .databaseCreator.fake .externalRequestFilter.fake
	mkdir -p ./findHataMessagingServer/libs
	cp ./webApiClient/build/libs/webApiClient-1.0-all.jar ./findHataMessagingServer/libs/
	cp ./databaseCreator/build/libs/databaseCreator-1.0-all.jar ./findHataMessagingServer/libs/
	cp ./result/build/libs/result-1.0-all.jar ./findHataMessagingServer/libs/
	cp ./externalRequestFilter/build/libs/externalRequestFilter-1.0-all.jar ./findHataMessagingServer/libs/
	cd ./findHataMessagingServer && ./gradlew copyJar

	touch .findHataMessagingServer.fake


.findHataProposalServer.fake: .result.fake .databaseCreator.fake .externalRequestFilter.fake .findHataUploadServer.fake
	mkdir -p ./findHataProposalServer/libs
	cp ./result/build/libs/result-1.0-all.jar ./findHataProposalServer/libs/
	cp ./databaseCreator/build/libs/databaseCreator-1.0-all.jar ./findHataProposalServer/libs/
	cp ./externalRequestFilter/build/libs/externalRequestFilter-1.0-all.jar ./findHataProposalServer/libs/

	cp ./findHataUploadServer/docker/findHataUploadServer-0.0.1-SNAPSHOT.jar ./findHataProposalServer/docker/findHataUploadServer-0.0.1-SNAPSHOT.jar

	cd ./findHataProposalServer && ./gradlew copyJar

	touch .findHataProposalServer.fake


.findHataServerDiscovery.fake:
	cd ./findHataServerDiscovery && ./gradlew copyJar

	touch .findHataServerDiscovery.fake

.findHataUploadServer.fake:
	cd ./findHataUploadServer && ./gradlew copyJar

	touch .findHataUploadServer.fake


