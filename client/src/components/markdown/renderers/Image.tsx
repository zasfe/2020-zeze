import React from "react";

interface IProps {
  src: string;
  alt: string;
  height: string;
  width: string;
}

const Image: React.FC<IProps> = ({src, alt, height, width}) => (
  <span className="image" style={{backgroundImage: `url("${src}")`, height, width}}/>
  // <img src={src} alt={alt} height={height} width={width}/>
);

export default Image;
