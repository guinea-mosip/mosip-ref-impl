all: package_app build_image run_app

.PHONY: all

package_app:
	npm run-script build -- --prod --base-href . --output-path=dist

build_image:
	docker build . --file Dockerfile --tag pre-registration-ui:dev

run_app:
	docker rm -f pre-registration-nginx-dev | true
	docker run --name pre-registration-nginx-dev -p 4242:80 -d pre-registration-ui:dev
