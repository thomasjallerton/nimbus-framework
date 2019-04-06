/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = props => (
      <div className="homeContainer">
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const Logo = props => (<div></div>);

    const ProjectTitle = () => (
      <h2 className="projectTitle" style={{color: '#373e48'}}>
        {siteConfig.title}
        <small>{siteConfig.tagline}</small>
      </h2>
    );

    const PromoSection = props => (
      <div className="section promoSection" >
        <div className="promoRow">
          <div className="pluginRowBlock" >{props.children}</div>
        </div>
      </div>
    );

    const Button = props => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" style={{color: '#373e48'}} href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    return (
      <SplashContainer>
        <Logo img_src={`${baseUrl}img/nimbus.svg`} />
        <div className="inner">
          <ProjectTitle siteConfig={siteConfig} />
          <PromoSection>
            <Button href="#try">Try It Out</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const FeatureCallout = () => (
      <div
        className="productShowcaseSection paddingBottom"
        style={{textAlign: 'center'}}>
        {/*<h2>Feature Callout</h2>
        <MarkdownBlock>These are features of this project</MarkdownBlock>*/}
      </div>
    );

    const TryOut = () => (
      <Block id="try">
        {[
          {
              content: 'Check out the [Getting Started](docs/gettingstarted.html) guide,' +
                  ' or jump straight into the [docs](docs/Introduction.html)',
            image: `${baseUrl}img/usenimbus.svg`,
            imageAlign: 'left',
            title: 'Try it Out!',
          },
        ]}
      </Block>
    );

    // const Description = () => (
    //   <Block background="light">
    //     {[
    //       {
    //         content:
    //           '',
    //         image: `${baseUrl}img/nimbus.svg`,
    //         imageAlign: 'right',
    //         title: 'Description',
    //       },
    //     ]}
    //   </Block>
    // );

    const Description = () => (
      <Block background="light">
        {[
          {
            content: 'Nimbus is a Java framework that lets you use annotations to describe your cloud ' +
                'resources, which can then be deployed to a cloud provider, or locally for testing',
            image: `${baseUrl}img/whatisnimbus.svg`,
            imageAlign: 'right',
            title: 'What is Nimbus?',
          },
        ]}
      </Block>
    );

    const Features = () => (
      <Block layout="fourColumn">
        {[
          {
            content: 'No more more massive configuration files, for ALL cloud resources',
            image: `${baseUrl}img/nimbus.svg`,
            imageAlign: 'top',
            title: 'Easy Cloud Deployment',
          },
          {
            content: 'Simulate deployments locally, for unit tests and localhost endpoints',
            image: `${baseUrl}img/local.svg`,
            imageAlign: 'top',
            title: 'Local Deployment and Testing',
          },
        ]}
      </Block>
    );

    const Showcase = () => {
      if ((siteConfig.users || []).length === 0) {
        return null;
      }

      const showcase = siteConfig.users
        .filter(user => user.pinned)
        .map(user => (
          <a href={user.infoLink} key={user.infoLink}>
            <img src={user.image} alt={user.caption} title={user.caption} />
          </a>
        ));

      const pageUrl = page => baseUrl + (language ? `${language}/` : '') + page;

      return (
        <div className="productShowcaseSection paddingBottom">
          <h2>Who is Using This?</h2>
          <p>This project is used by all these people</p>
          <div className="logos">{showcase}</div>
          <div className="more-users">
            <a className="button" href={pageUrl('users.html')}>
              More {siteConfig.title} Users
            </a>
          </div>
        </div>
      );
    };

    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          <Features />
          <Description />
          <TryOut />
        </div>
      </div>
    );
  }
}

module.exports = Index;
