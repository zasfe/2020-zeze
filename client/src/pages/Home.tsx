import React, {useEffect} from "react";
import styled from "@emotion/styled";
import GlobalLayout from "../components/common/GlobalLayout";
import {GITHUB_AUTH_URL, MAX_WIDTH, MOBILE_MAX_WIDTH, ZEZE_GRAY} from "../domains/constants";
import Carousel from "../components/common/Carousel";
import SlideExample from "../components/common/SlideExample";
import {googleAnalyticsEvent, googleAnalyticsPageView} from "../utils/googleAnalytics";

export const HomeBlock = styled.div`
  background-color: ${ZEZE_GRAY};
`;

export const Layout = styled.main`
  padding: 15px;
  max-width: ${MAX_WIDTH}px;
  margin: 0 auto;
`;

export const Slogan = styled.h2`
  margin: 0;
  color: #fff;
  font-size: 6rem;
  
  @media (max-width: ${MOBILE_MAX_WIDTH}px) {
    font-size: 2.5rem;
  }
`;

interface SectionProps {
  background: string;
}

export const Section = styled("div")<SectionProps>`
  background-color: ${({background}: SectionProps) => background};
  padding: 60px 15px;
  margin: 0 auto;
  
  @media (max-width: ${MOBILE_MAX_WIDTH}px) {
    padding: 30px 15px;
  }
`;

export const Button = styled.a`
  text-decoration: none;
  display: inline-block;
  border: 2px solid white;
  border-radius: 50px;
  padding: 1rem 1.5rem;
  margin: 2rem 0 1.5rem;
  color: #fff;
  font-weight: 600;
  font-size: 1.4rem;
  
  &:hover {
    cursor: pointer;
  }
  
  @media (max-width: ${MOBILE_MAX_WIDTH}px) {
    margin: 1rem 0 0.75rem;
    font-size: 1.1rem;
  }
`;

const Slide = styled.div`
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
  height: 30rem;
  
  @media (max-width: ${MOBILE_MAX_WIDTH}px) {
    height: 15rem;
    font-size: 0.5em;
  }
`;

const MetaData = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  h1, h2, h3, h4 {
    border: none;
    margin: 10px;
    padding: 0 30px;
  }
  
  h1 {
    font-size: 4em;
  }
  
  h2 {
    font-size: 2.5em;
    color: #777; 
  }
  
  h3 {
    margin-top: 1em;
  }
`;

const Home: React.FC = () => {
  useEffect(() => {
    googleAnalyticsPageView("Landing");
  }, []);

  return (
    <GlobalLayout>
      <HomeBlock>
        <Section background={ZEZE_GRAY}>
          <Layout>
            <Slogan>Beautiful,</Slogan>
            <Slogan>Minimal <span style={{color: "#00FF7F"}}>Slides</span></Slogan>
            <Slogan>With Markdown</Slogan>
            <Button href={GITHUB_AUTH_URL} onClick={() => googleAnalyticsEvent("User", "Get Started")}>Get Started for Free →</Button>
          </Layout>
        </Section>
        <Section background="#333">
          <Layout>
            <code style={{color: "#fff"}}>
              --- <br/>
              title: Hello Slide.md! <br/>
              subtitle: Introducing markdown slides <br/>
              author: Hodol <br/>
              createdAt: 2020-07-12 <br/>
              --- <br/> <br/>
              ## Works like charm <br/>
              ### with minimal effort <br/> <br/>
              - Only need to type <br/>
              - Supports GFM Markdown <br/>
              - Youtube, Charts, and more! <br/> <br/>
              --- <br/> <br/>
              ## Focus on your idea <br/> <br/>
              > No more decorating stuff <br/>
              > Pixel perfect beautiful slides
            </code>
          </Layout>
        </Section>
        <Section background={ZEZE_GRAY}>
          <Slide>
            <Carousel>
              <MetaData>
                <h1>Hello Slide.md!</h1>
                <h2>Introducing markdown slides!</h2>
                <h3>Hodol</h3>
                <h4>2020-07-12</h4>
              </MetaData>
              <SlideExample
                content={`## Works like charm \n ### with minimal effort \n\n - Only need to type \n - Supports GFM Markdown \n - Youtube, Charts, and more! \n\n`}/>
              <SlideExample
                content={`## Focus on your idea \n\n > No more decorating stuff \n\n > Pixel perfect beautiful slides`}/>
            </Carousel>
          </Slide>
        </Section>
      </HomeBlock>
    </GlobalLayout>
  );
};

export default Home;
