/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
    docUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        const docsUrl = this.props.config.docsUrl;
        const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
        const langPart = '';
        return `${baseUrl}${docsPart}${langPart}${doc}`;
    }

    pageUrl(doc, language) {
        const baseUrl = this.props.config.baseUrl;
        return baseUrl + (language ? `${language}/` : '') + doc;
    }

    render() {
        return (
            <footer className="nav-footer" id="footer">
                <section className="sitemap">
                    <a href={this.props.config.baseUrl} className="nav-home">
                        {this.props.config.footerIcon && (
                            <img
                                src={this.props.config.baseUrl + this.props.config.footerIcon}
                                alt={this.props.config.title}
                                width="66"
                                height="58"
                            />
                        )}
                    </a>
                    <div>
                        <h5>Docs</h5>
                        <a href={this.docUrl('gettingstarted.html', this.props.language)}>
                            Getting Started
                        </a>
                        <a href={this.docUrl('Introduction.html', this.props.language)}>
                            Documentation
                        </a>
                    </div>
                    <div></div>
                    <div>
                        <h5>More</h5>
                        <a href="https://github.com/thomasjallerton/nimbus-core">GitHub</a>
                        <a
                            className="github-button"
                            href={this.props.config.repoUrl}
                            data-icon="octicon-star"
                            data-show-count="false"
                            aria-label="Star this project on GitHub">
                            Star
                        </a>
                    </div>
                </section>
                <section className="copyright">{this.props.config.copyright}</section>
            </footer>
        );
    }
}

module.exports = Footer;
