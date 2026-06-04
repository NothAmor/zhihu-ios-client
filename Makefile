.PHONY: all shared-framework shared-framework-release xcode-build clean help

# Default target
all: help

# Build the KMP shared framework for iOS Simulator (debug)
shared-framework:
	./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Build the KMP shared framework for iOS Simulator (release)
shared-framework-release:
	./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64

# Build for physical device
shared-framework-device:
	./gradlew :shared:linkDebugFrameworkIosArm64

# Open Xcode to build and run the app
xcode-open:
	open ZhihuApp/ZhihuApp.xcodeproj

# Build Xcode project
xcode-build: shared-framework
	xcodebuild -project ZhihuApp/ZhihuApp.xcodeproj \
		-scheme ZhihuApp \
		-configuration Debug \
		-sdk iphonesimulator \
		-destination 'platform=iOS Simulator,name=iPhone 16' \
		build

# Clean all build artifacts
clean:
	./gradlew clean
	rm -rf ZhihuApp/DerivedData

# Show help
help:
	@echo "Zhihu++ iOS Build Targets:"
	@echo ""
	@echo "  make shared-framework        Build KMP framework for iOS Simulator (debug)"
	@echo "  make shared-framework-device Build KMP framework for iOS device"
	@echo "  make xcode-open              Open Xcode project"
	@echo "  make xcode-build             Build Xcode project (builds framework first)"
	@echo "  make clean                   Clean all build artifacts"
	@echo ""
	@echo "Prerequisites:"
	@echo "  - Java 17+ (brew install openjdk@17)"
	@echo "  - Xcode 16+"
	@echo "  - Android SDK (for androidMain compilation only; optional for pure iOS)"
