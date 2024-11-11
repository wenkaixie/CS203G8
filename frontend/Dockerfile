# Use a Node.js base image to build the React app
FROM node:20 as build

# Set the working directory in the container
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json ./
RUN npm install

# Copy the .env.production file into the container
COPY .env.production .env

# Copy the rest of the application files
COPY . .

# Build the React app for production
RUN npm run build

# Use a lightweight Nginx image to serve the static files
FROM nginx:alpine

# Copy the built files from the previous stage to the Nginx container
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 3000

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]