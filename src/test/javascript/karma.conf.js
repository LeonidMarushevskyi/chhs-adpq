// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html

module.exports = function (config) {
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '../../',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            // bower:js
            'main/webapp/bower_components/jquery/dist/jquery.js',
            'main/webapp/bower_components/angular/angular.js',
            'main/webapp/bower_components/angular-aria/angular-aria.js',
            'main/webapp/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
            'main/webapp/bower_components/angular-cache-buster/angular-cache-buster.js',
            'main/webapp/bower_components/angular-cookies/angular-cookies.js',
            'main/webapp/bower_components/angular-dynamic-locale/src/tmhDynamicLocale.js',
            'main/webapp/bower_components/angular-local-storage/dist/angular-local-storage.js',
            'main/webapp/bower_components/angular-loading-bar/build/loading-bar.js',
            'main/webapp/bower_components/angular-resource/angular-resource.js',
            'main/webapp/bower_components/angular-sanitize/angular-sanitize.js',
            'main/webapp/bower_components/angular-translate/angular-translate.js',
            'main/webapp/bower_components/messageformat/messageformat.js',
            'main/webapp/bower_components/angular-translate-interpolation-messageformat/angular-translate-interpolation-messageformat.js',
            'main/webapp/bower_components/angular-translate-loader-partial/angular-translate-loader-partial.js',
            'main/webapp/bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.js',
            'main/webapp/bower_components/angular-ui-router/release/angular-ui-router.js',
            'main/webapp/bower_components/bootstrap-sass/assets/javascripts/bootstrap.js',
            'main/webapp/bower_components/json3/lib/json3.js',
            'main/webapp/bower_components/ng-file-upload/ng-file-upload.js',
            'main/webapp/bower_components/ngInfiniteScroll/build/ng-infinite-scroll.js',
            'main/webapp/bower_components/sockjs-client/dist/sockjs.js',
            'main/webapp/bower_components/stomp-websocket/lib/stomp.min.js',
            'main/webapp/bower_components/angular-object-diff/dist/angular-object-diff.js',
            'main/webapp/bower_components/bootstrap/dist/js/bootstrap.js',
            'main/webapp/bower_components/bootstrap-switch/dist/js/bootstrap-switch.js',
            'main/webapp/bower_components/ui-select/dist/select.js',
            'main/webapp/bower_components/angular-ui-mask/dist/mask.js',
            'main/webapp/bower_components/jquery-mousewheel/jquery.mousewheel.js',
            'main/webapp/bower_components/malihu-custom-scrollbar-plugin/jquery.mCustomScrollbar.js',
            'main/webapp/bower_components/ng-scrollbars/dist/scrollbars.min.js',
            'main/webapp/bower_components/fuse.js/src/fuse.js',
            'main/webapp/bower_components/jquery-emulatetab/src/emulatetab.joelpurra.js',
            'main/webapp/bower_components/jQuery-contextMenu/dist/jquery.contextMenu.js',
            'main/webapp/bower_components/jQuery-contextMenu/dist/jquery.contextMenu.min.js',
            'main/webapp/bower_components/jQuery-contextMenu/dist/jquery.ui.position.js',
            'main/webapp/bower_components/jQuery-contextMenu/dist/jquery.ui.position.min.js',
            'main/webapp/bower_components/matchmedia/matchMedia.js',
            'main/webapp/bower_components/ngSticky/lib/sticky.js',
            'main/webapp/bower_components/moment/moment.js',
            'main/webapp/bower_components/fullcalendar/dist/fullcalendar.js',
            'main/webapp/bower_components/bootstrap-ui-datetime-picker/dist/datetime-picker.js',
            'main/webapp/bower_components/angular-simple-logger/dist/angular-simple-logger.js',
            'main/webapp/bower_components/leaflet/dist/leaflet-src.js',
            'main/webapp/bower_components/ui-leaflet/dist/ui-leaflet.js',
            'main/webapp/bower_components/lodash/lodash.js',
            'main/webapp/bower_components/leaflet.markercluster/dist/leaflet.markercluster-src.js',
            'main/webapp/bower_components/angular-animate/angular-animate.js',
            'main/webapp/bower_components/angular-mocks/angular-mocks.js',
            // endbower

            'main/webapp/scripts/app/app.js',
            'main/webapp/scripts/app/**/*.js',
            'main/webapp/scripts/components/**/*.+(js|html)',
            'test/javascript/spec/helpers/module.js',
            'test/javascript/spec/helpers/httpBackend.js',
            'test/javascript/**/!(karma.conf|protractor.conf).js',
            'main/webapp/bower_components/ngToast/dist/ngToast.min.js'
        ],


        // list of files / patterns to exclude
        exclude: ['test/javascript/e2e/**', 'test/javascript/spec/app/account/register/**', 'test/javascript/spec/app/account/settings/**'],

        preprocessors: {
            './**/*.js': ['coverage']
        },

        reporters: ['dots', 'jenkins', 'coverage', 'progress'],

        jenkinsReporter: {

            outputFile: '../target/test-results/karma/TESTS-results.xml'
        },

        coverageReporter: {

            dir: '../target/test-results/coverage',
            reporters: [
                {type: 'lcov', subdir: 'report-lcov'}
            ]
        },

        // web server port
        port: 9876,

        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],

        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false,

        // to avoid DISCONNECTED messages when connecting to slow virtual machines
        browserDisconnectTimeout : 10000, // default 2000
        browserDisconnectTolerance : 1, // default 0
        browserNoActivityTimeout : 4*60*1000 //default 10000
    });
};
