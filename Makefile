build: build-all-jar
build-in-docker: build-all-jar-in-docker

clean: clean-docker clean-all-files

clean-docker:
	sudo docker compose down --volumes
	sudo docker rmi -f findhata-gateway-server findhata-auth-server findhata-notification-server findhata-discovery-server findhata-data-base findhata-messaging-server findhata-proposal-server findhata-frontend-server findhata-ingress findhata-tunnel findhata-vectorization-server findhata-upload-server findhata-web-api-client findhata-external-request-filter findhata-token-service findhata-result findhata-database-creator

build-all-jar: .findHataAuth.fake .findHataGateway.fake .findHataNotificationServer.fake .findHataMessagingServer.fake .findHataProposalServer.fake .findHataServerDiscovery.fake
	sudo docker compose build


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


